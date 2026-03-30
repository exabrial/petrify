package com.github.exabrial.petrify;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeBuilder.BlockCodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import com.github.exabrial.petrify.internal.model.Stratum;
import com.github.exabrial.petrify.model.LeafClassEntry;
import com.github.exabrial.petrify.model.Fossil;
import com.github.exabrial.petrify.model.Grove;
import com.github.exabrial.petrify.model.PetrifyConstants;
import com.github.exabrial.petrify.model.exception.UnexpectedCometImpact;

public class Petrify {
	public static final String PETRIFIED_FOSSIL = "PetrifiedFossil$Petrify0x";
	public static final int JDK_17 = 61;

	protected static final int SLOT_THIS = 0;
	protected static final int SLOT_FEATURES = 1;
	protected static final int SLOT_SCORES = 2;

	protected static int counter = 0;

	public Fossil fossilize(final MethodHandles.Lookup lookup, final Grove grove) {
		try {
			final Stratum stratum = new Stratum(grove);

			final byte[] fossilBytes = ClassFile.of().build(
					ClassDesc.of(lookup.lookupClass().getPackageName(), PETRIFIED_FOSSIL + Integer.toHexString(counter++)),
					(final ClassBuilder classBuilder) -> {
						setJdk(classBuilder);
						implementFossilInterface(classBuilder);
						createDefaultConstructor(classBuilder);
						implementPredictMethod(classBuilder, stratum);
					});

			final Class<?> clazz = lookup.defineClass(fossilBytes);
			final Fossil fossil = (Fossil) clazz.getDeclaredConstructor().newInstance();
			return fossil;
		} catch (final Exception e) {
			throw new UnexpectedCometImpact(e);
		}
	}

	protected void implementPredictMethod(final ClassBuilder classBuilder, final Stratum stratum) {
		classBuilder.withMethodBody(Fossil.predict, MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_float.arrayType()),
				ClassFile.ACC_PUBLIC, (final CodeBuilder codeBuilder) -> {
					// Create per-class score accumulator floats
					codeBuilder.ldc(stratum.grove.getClassLabelsInt64s().length);
					codeBuilder.newarray(TypeKind.FLOAT);
					codeBuilder.astore(SLOT_SCORES);

					// Walk each tree; emitLeaf accumulates weights into scores[classId]
					for (final int treeId : stratum.grove.getTreeRootIds()) {
						final int rootArrayIdx = stratum.nodeIndex.get(PetrifyConstants.packLong(treeId, 0));
						emitTree(codeBuilder, stratum, treeId, rootArrayIdx);
					}

					// Prep and call fossil.classify(scores, postTransform, isBinarySingleScore)
					codeBuilder.aload(SLOT_THIS);
					codeBuilder.aload(SLOT_SCORES);
					codeBuilder.ldc((int) stratum.grove.getPostTransform());
					codeBuilder.ldc(stratum.isBinarySingleScore ? 1 : 0);
					codeBuilder.invokeinterface(ClassDesc.of(Fossil.class.getPackageName(), Fossil.class.getSimpleName()), Fossil.classify,
							MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_float.arrayType(), ConstantDescs.CD_byte,
									ConstantDescs.CD_boolean));
					// Winning class array index is now on the stack

					// Lookup the class array index in the classLabels
					final long[] classLabels = stratum.grove.getClassLabelsInt64s();
					emitClassLabelLookup(codeBuilder, classLabels);

					// Return the value from classLabel lookup to the callee. We did it!
					codeBuilder.ireturn();
				});
	}

	protected void emitTree(final CodeBuilder codeBuilder, final Stratum stratum, final int treeId, final int arrayIdx) {
		final byte mode = stratum.grove.getNodesModes()[arrayIdx];
		switch (mode) {

			case PetrifyConstants.MODE_BRANCH_LEQ, PetrifyConstants.MODE_BRANCH_LT -> {
				emitBranch(codeBuilder, stratum, treeId, arrayIdx);
			}

			case PetrifyConstants.MODE_LEAF -> {
				emitLeaf(codeBuilder, stratum, treeId, arrayIdx);
			}

			default -> {
				throw new UnexpectedCometImpact("Unknown tree mode: " + mode);
			}
		}
	}

	protected void emitBranch(final CodeBuilder codeBuilder, final Stratum stratum, final int treeId, final int arrayIdx) {
		final int featureId = stratum.grove.getNodesFeatureIds()[arrayIdx];
		final float threshold = stratum.grove.getNodesValues()[arrayIdx];

		// Load features[featureId] onto stack
		codeBuilder.aload(SLOT_FEATURES);
		codeBuilder.ldc(featureId);

		// Load threshold onto stack
		codeBuilder.faload();
		codeBuilder.ldc(threshold);

		// Actually compare the feature to the threshold; BUT if something is missing/NaN, follow the "missing" policy
		final int missingTracksTrue = stratum.grove.getNodesMissingValueTracksTrue()[arrayIdx];
		if (missingTracksTrue == 0) {
			codeBuilder.fcmpg();
		} else {
			codeBuilder.fcmpl();
		}
		// We now have one of {-1, 0, 1} on the stack

		final byte mode = stratum.grove.getNodesModes()[arrayIdx];
		// Compute the correct opcode for the upcoming ifThenElse
		final Opcode branchOpcode = switch (mode) {
			// feature <= threshold (stack is -1 or 0)
			case PetrifyConstants.MODE_BRANCH_LEQ -> {
				// IFGT jumps when stack > 0 (stack is 1), which is the false branch
				yield Opcode.IFGT;
			}

			// feature < threshold (stack is -1)
			case PetrifyConstants.MODE_BRANCH_LT -> {
				// IFGE jumps when stack >= 0 (stack is 0 or 1), which is the false branch
				yield Opcode.IFGE;
			}

			default -> {
				throw new UnexpectedCometImpact("Unknown branch mode: " + mode);
			}
		};

		final int trueNodeId = stratum.grove.getNodesTrueNodeIds()[arrayIdx];
		final int trueArrayIdx = stratum.nodeIndex.get(PetrifyConstants.packLong(treeId, trueNodeId));

		final int falseNodeId = stratum.grove.getNodesFalseNodeIds()[arrayIdx];
		final int falseArrayIdx = stratum.nodeIndex.get(PetrifyConstants.packLong(treeId, falseNodeId));

		codeBuilder.ifThenElse(branchOpcode, (final BlockCodeBuilder falseBlock) -> {
			emitTree(falseBlock, stratum, treeId, falseArrayIdx);
		}, (final BlockCodeBuilder trueBlock) -> {
			emitTree(trueBlock, stratum, treeId, trueArrayIdx);
		});
	}

	protected void emitLeaf(final CodeBuilder codeBuilder, final Stratum stratum, final int treeId, final int arrayIdx) {
		final int nodeId = stratum.grove.getNodesNodeIds()[arrayIdx];
		final long key = PetrifyConstants.packLong(treeId, nodeId);
		for (final LeafClassEntry entry : stratum.leafClassEntries.get(key)) {
			// implement: scores[classId] += weight

			// Load scores array ref and classId index
			codeBuilder.aload(SLOT_SCORES);
			codeBuilder.ldc(entry.classId());
			// Duplicate scores ref and classId for the final fastore
			codeBuilder.dup2();

			// Read the current value at scores[classId]
			codeBuilder.faload();
			// Grab the current leaf weight
			codeBuilder.ldc(entry.weight());
			// Add the leaf weight to the current value
			codeBuilder.fadd();
			// Store the add result back into the array
			codeBuilder.fastore();
		}
	}

	protected void emitClassLabelLookup(final CodeBuilder codeBuilder, final long[] classLabels) {
		// Stack has the class index (int) from fossil.classify(..) invocation.

		// Use a tableswitch to map index -> class label constant
		final Label defaultLabel = codeBuilder.newLabel();
		final Label endLabel = codeBuilder.newLabel();
		final Label[] caseLabels = new Label[classLabels.length];
		for (int i = 0; i < classLabels.length; i++) {
			caseLabels[i] = codeBuilder.newLabel();
		}

		// Build the SwitchCase list
		final List<SwitchCase> cases = new ArrayList<>();
		for (int caseLabelIdx = 0; caseLabelIdx < classLabels.length; caseLabelIdx++) {
			cases.add(SwitchCase.of(caseLabelIdx, caseLabels[caseLabelIdx]));
		}

		// Emit the tableswitch (pops the index from the stack)
		codeBuilder.tableswitch(0, classLabels.length - 1, defaultLabel, cases);

		// Emit each case: push the label constant and jump to end
		for (int i = 0; i < classLabels.length; i++) {
			codeBuilder.labelBinding(caseLabels[i]);
			codeBuilder.ldc((int) classLabels[i]);
			codeBuilder.goto_(endLabel);
		}

		// Default case (shouldn't happen, but required by tableswitch): push first label
		codeBuilder.labelBinding(defaultLabel);
		codeBuilder.ldc((int) classLabels[0]);

		codeBuilder.labelBinding(endLabel);
	}

	protected void setJdk(final ClassBuilder classBuilder) {
		classBuilder.withVersion(JDK_17, 0);
	}

	protected void implementFossilInterface(final ClassBuilder classBuilder) {
		classBuilder.withInterfaceSymbols(ClassDesc.of(Fossil.class.getPackageName(), Fossil.class.getSimpleName()));
	}

	protected void createDefaultConstructor(final ClassBuilder classBuilder) {
		classBuilder.withMethodBody(ConstantDescs.INIT_NAME, ConstantDescs.MTD_void, ClassFile.ACC_PUBLIC,
				(final CodeBuilder initCodeBuilder) -> {
					initCodeBuilder.aload(0);
					initCodeBuilder.invokespecial(ConstantDescs.CD_Object, ConstantDescs.INIT_NAME, ConstantDescs.MTD_void);
					initCodeBuilder.return_();
				});
	}
}
