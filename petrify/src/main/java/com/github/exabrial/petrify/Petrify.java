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

import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
import com.github.exabrial.petrify.compiler.model.LeafClassEntry;
import com.github.exabrial.petrify.compiler.model.LeafTargetEntry;
import com.github.exabrial.petrify.compiler.model.RegressorGrove;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedCometImpact;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedTreeBranch;
import com.github.exabrial.petrify.internal.model.ClassifierStratum;
import com.github.exabrial.petrify.internal.model.RegressorStratum;
import com.github.exabrial.petrify.internal.model.Stratum;
import com.github.exabrial.petrify.model.ClassifierFossil;
import com.github.exabrial.petrify.model.Fossil;
import com.github.exabrial.petrify.model.PetrifyConstants;
import com.github.exabrial.petrify.model.RegressionFossil;

public class Petrify {
	public static final String PETRIFIED_FOSSIL = "PetrifiedFossil$Petrify0x";
	public static final int JDK_17 = 61;

	protected static final int SLOT_THIS = 0;
	protected static final int SLOT_FEATURES = 1;
	protected static final int SLOT_SCORES = 2;
	protected static final String TREE_METHOD_PREFIX = "tree_";

	protected static int counter = 0;

	public ClassifierFossil fossilize(final MethodHandles.Lookup lookup, final ClassifierGrove grove) {
		try {
			final ClassifierStratum stratum = new ClassifierStratum(grove);

			final ClassDesc thisClass = ClassDesc.of(lookup.lookupClass().getPackageName(),
					PETRIFIED_FOSSIL + Integer.toHexString(counter++));
			final byte[] fossilBytes = ClassFile.of().build(thisClass, (final ClassBuilder classBuilder) -> {
				setJdk(classBuilder);
				implementFossilInterface(classBuilder, ClassifierFossil.class);
				createDefaultConstructor(classBuilder);
				createMethodPerEnsemble(classBuilder, stratum);
				implementClassifierPredictMethod(classBuilder, stratum, thisClass);
			});

			final Class<?> clazz = lookup.defineClass(fossilBytes);
			final ClassifierFossil fossil = (ClassifierFossil) clazz.getDeclaredConstructor().newInstance();
			return fossil;
		} catch (final Exception e) {
			throw new UnexpectedCometImpact(e);
		}
	}

	public RegressionFossil fossilize(final MethodHandles.Lookup lookup, final RegressorGrove grove) {
		try {
			final RegressorStratum stratum = new RegressorStratum(grove);

			final ClassDesc thisClass = ClassDesc.of(lookup.lookupClass().getPackageName(),
					PETRIFIED_FOSSIL + Integer.toHexString(counter++));
			final byte[] fossilBytes = ClassFile.of().build(thisClass, (final ClassBuilder classBuilder) -> {
				setJdk(classBuilder);
				implementFossilInterface(classBuilder, RegressionFossil.class);
				createDefaultConstructor(classBuilder);
				createMethodPerEnsemble(classBuilder, stratum);
				implementRegressorPredictMethod(classBuilder, stratum, thisClass);
			});

			final Class<?> clazz = lookup.defineClass(fossilBytes);
			final RegressionFossil fossil = (RegressionFossil) clazz.getDeclaredConstructor().newInstance();
			return fossil;
		} catch (final Exception e) {
			throw new UnexpectedCometImpact(e);
		}
	}

	protected void implementClassifierPredictMethod(final ClassBuilder classBuilder, final ClassifierStratum stratum,
			final ClassDesc thisClass) {
		final MethodTypeDesc treeMethodDesc = MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_float.arrayType(),
				ConstantDescs.CD_float.arrayType());

		classBuilder.withMethodBody(ClassifierFossil.predict, MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_float.arrayType()),
				ClassFile.ACC_PUBLIC, (final CodeBuilder codeBuilder) -> {
					// Create per-class score accumulator floats
					codeBuilder.ldc(stratum.classifierGrove.getClassLabelsInt64s().length);
					codeBuilder.newarray(TypeKind.FLOAT);
					codeBuilder.astore(SLOT_SCORES);

					// Invoke each tree method: this.tree_N(features, scores)
					emitTreeInvocations(codeBuilder, stratum, thisClass, treeMethodDesc);

					// Apply per-class bias before post-transform
					emitBaseValues(codeBuilder, stratum.grove.getBaseValues());

					// Prep and call fossil.classify(scores, postTransform, isBinarySingleScore)
					codeBuilder.aload(SLOT_THIS);
					codeBuilder.aload(SLOT_SCORES);
					codeBuilder.ldc((int) stratum.grove.getPostTransform());
					codeBuilder.ldc(stratum.isBinarySingleScore ? 1 : 0);
					codeBuilder.invokeinterface(ClassDesc.of(ClassifierFossil.class.getPackageName(), ClassifierFossil.class.getSimpleName()),
							ClassifierFossil.classify, MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_float.arrayType(),
									ConstantDescs.CD_byte, ConstantDescs.CD_boolean));
					// Winning class array index is now on the stack

					// Lookup the class array index in the classLabels
					final long[] classLabels = stratum.classifierGrove.getClassLabelsInt64s();
					emitClassLabelLookup(codeBuilder, classLabels);

					// Return the value from classLabel lookup to the callee
					codeBuilder.ireturn();
				});
	}

	protected void implementRegressorPredictMethod(final ClassBuilder classBuilder, final RegressorStratum stratum,
			final ClassDesc thisClass) {
		final MethodTypeDesc treeMethodDesc = MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_float.arrayType(),
				ConstantDescs.CD_float.arrayType());

		classBuilder.withMethodBody(RegressionFossil.predict,
				MethodTypeDesc.of(ConstantDescs.CD_float, ConstantDescs.CD_float.arrayType()), ClassFile.ACC_PUBLIC,
				(final CodeBuilder codeBuilder) -> {
					// Create single-element score accumulator (single-target regression)
					codeBuilder.ldc(stratum.regressorGrove.getNTargets());
					codeBuilder.newarray(TypeKind.FLOAT);
					codeBuilder.astore(SLOT_SCORES);

					// Invoke each tree method: this.tree_N(features, scores)
					emitTreeInvocations(codeBuilder, stratum, thisClass, treeMethodDesc);

					// Apply base values bias
					emitBaseValues(codeBuilder, stratum.grove.getBaseValues());

					// Load scores[0] and call fossil.aggregate(score, postTransform)
					codeBuilder.aload(SLOT_SCORES);
					codeBuilder.ldc(0);
					codeBuilder.faload();
					// score is now on the stack

					codeBuilder.aload(SLOT_THIS);
					codeBuilder.swap();
					codeBuilder.ldc((int) stratum.grove.getPostTransform());
					codeBuilder.invokeinterface(ClassDesc.of(RegressionFossil.class.getPackageName(), RegressionFossil.class.getSimpleName()),
							RegressionFossil.aggregate, MethodTypeDesc.of(ConstantDescs.CD_float, ConstantDescs.CD_float, ConstantDescs.CD_byte));

					// Return the transformed score
					codeBuilder.freturn();
				});
	}

	protected void emitClassifierLeaf(final CodeBuilder codeBuilder, final ClassifierStratum stratum, final int treeId,
			final int arrayIdx) {
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

	protected void emitRegressorLeaf(final CodeBuilder codeBuilder, final RegressorStratum stratum, final int treeId,
			final int arrayIdx) {
		final int nodeId = stratum.grove.getNodesNodeIds()[arrayIdx];
		final long key = PetrifyConstants.packLong(treeId, nodeId);
		for (final LeafTargetEntry entry : stratum.leafTargetEntries.get(key)) {
			// implement: scores[targetId] += weight

			// Load scores array ref and targetId index
			codeBuilder.aload(SLOT_SCORES);
			codeBuilder.ldc(entry.targetId());
			// Duplicate scores ref and targetId for the final fastore
			codeBuilder.dup2();

			// Read the current value at scores[targetId]
			codeBuilder.faload();
			// Grab the current leaf weight
			codeBuilder.ldc(entry.weight());
			// Add the leaf weight to the current value
			codeBuilder.fadd();
			// Store the add result back into the array
			codeBuilder.fastore();
		}
	}

	protected void createMethodPerEnsemble(final ClassBuilder classBuilder, final Stratum stratum) {
		final MethodTypeDesc treeMethodDesc = MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_float.arrayType(),
				ConstantDescs.CD_float.arrayType());
		for (final int treeId : stratum.grove.getTreeRootIds()) {
			final int rootArrayIdx = stratum.nodeIndex.get(PetrifyConstants.packLong(treeId, 0));
			emitTreeMethod(classBuilder, treeMethodDesc, stratum, treeId, rootArrayIdx);
		}
	}

	protected void emitTreeInvocations(final CodeBuilder codeBuilder, final Stratum stratum, final ClassDesc thisClass,
			final MethodTypeDesc treeMethodDesc) {
		for (final int treeId : stratum.grove.getTreeRootIds()) {
			codeBuilder.aload(SLOT_THIS);
			codeBuilder.aload(SLOT_FEATURES);
			codeBuilder.aload(SLOT_SCORES);
			codeBuilder.invokevirtual(thisClass, TREE_METHOD_PREFIX + treeId, treeMethodDesc);
		}
	}

	protected void emitTreeMethod(final ClassBuilder classBuilder, final MethodTypeDesc treeMethodDesc, final Stratum stratum,
			final int treeId, final int rootArrayIdx) {
		// Emit a tree as a private method: tree_N(float[] features, float[] scores) -> void

		// Slot layout matches predict(): this=0, features=1, scores=2
		classBuilder.withMethodBody(TREE_METHOD_PREFIX + treeId, treeMethodDesc, ClassFile.ACC_PRIVATE,
				(final CodeBuilder codeBuilder) -> {
					emitTree(codeBuilder, stratum, treeId, rootArrayIdx);
					codeBuilder.return_();
				});
	}

	protected void emitTree(final CodeBuilder codeBuilder, final Stratum stratum, final int treeId, final int arrayIdx) {
		final byte mode = stratum.grove.getNodesModes()[arrayIdx];
		switch (mode) {

			case PetrifyConstants.MODE_BRANCH_LEQ, PetrifyConstants.MODE_BRANCH_LT, PetrifyConstants.MODE_BRANCH_GEQ, PetrifyConstants.MODE_BRANCH_GT -> {
				emitBranch(codeBuilder, stratum, treeId, arrayIdx);
			}

			case PetrifyConstants.MODE_LEAF -> {
				switch (stratum) {
					case final ClassifierStratum classifierStratum -> {
						emitClassifierLeaf(codeBuilder, classifierStratum, treeId, arrayIdx);
					}
					case final RegressorStratum regressorStratum -> {
						emitRegressorLeaf(codeBuilder, regressorStratum, treeId, arrayIdx);
					}
					default -> {
						throw new UnexpectedTreeBranch("Unknown stratum type: " + stratum.getClass().getName());
					}
				}
			}

			default -> {
				throw new UnexpectedTreeBranch("Unknown tree mode: " + mode);
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

		// Actually compare the feature to the threshold; BUT if something is missing/NaN, follow the "missing" policy. For LEQ/LT:
		// NaN-to-false needs fcmpg (NaN->1, jumps to false). NaN-to-true needs fcmpl (NaN->-1, stays in true). For GEQ/GT: NaN-to-false
		// needs fcmpl (NaN->-1, jumps to false). NaN-to-true needs fcmpg (NaN->1, stays in true). In other words, GT/GEQ invert the
		// fcmpg/fcmpl polarity relative to LEQ/LT.
		final byte mode = stratum.grove.getNodesModes()[arrayIdx];
		final int missingTracksTrue = stratum.grove.getNodesMissingValueTracksTrue()[arrayIdx];
		final boolean invertNanPolarity = mode == PetrifyConstants.MODE_BRANCH_GT || mode == PetrifyConstants.MODE_BRANCH_GEQ;
		if (missingTracksTrue == 0 != invertNanPolarity) {
			codeBuilder.fcmpg();
		} else {
			codeBuilder.fcmpl();
		}
		// We now have one of {-1, 0, 1} on the stack

		// Compute the correct opcode for the upcoming ifThenElse
		final Opcode branchOpcode = switch (mode) {
			// feature <= threshold: true when stack is -1 or 0, false when 1
			case PetrifyConstants.MODE_BRANCH_LEQ -> {
				// IFGT jumps when stack > 0 (stack is 1), which is the false branch
				yield Opcode.IFGT;
			}

			// feature < threshold: true when stack is -1, false when 0 or 1
			case PetrifyConstants.MODE_BRANCH_LT -> {
				// IFGE jumps when stack >= 0 (stack is 0 or 1), which is the false branch
				yield Opcode.IFGE;
			}

			// feature >= threshold: true when stack is 0 or 1, false when -1
			case PetrifyConstants.MODE_BRANCH_GEQ -> {
				// IFLT jumps when stack < 0 (stack is -1), which is the false branch
				yield Opcode.IFLT;
			}

			// feature > threshold: true when stack is 1, false when -1 or 0
			case PetrifyConstants.MODE_BRANCH_GT -> {
				// IFLE jumps when stack <= 0 (stack is -1 or 0), which is the false branch
				yield Opcode.IFLE;
			}

			default -> {
				throw new UnexpectedTreeBranch("Unknown branch mode: " + mode);
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

	protected void emitBaseValues(final CodeBuilder codeBuilder, final float[] baseValues) {
		if (baseValues == null) {
			return;
		} else {
			for (int classIdx = 0; classIdx < baseValues.length; classIdx++) {
				if (baseValues[classIdx] != 0.0f) {
					// scores[idx] += baseValue
					codeBuilder.aload(SLOT_SCORES);
					codeBuilder.ldc(classIdx);
					codeBuilder.dup2();

					codeBuilder.faload();
					codeBuilder.ldc(baseValues[classIdx]);
					codeBuilder.fadd();
					codeBuilder.fastore();
				}
			}
		}
	}

	protected void setJdk(final ClassBuilder classBuilder) {
		classBuilder.withVersion(JDK_17, 0);
	}

	protected void implementFossilInterface(final ClassBuilder classBuilder, final Class<? extends Fossil> fossilType) {
		classBuilder.withInterfaceSymbols(ClassDesc.of(fossilType.getPackageName(), fossilType.getSimpleName()));
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
