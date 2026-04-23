package com.github.exabrial.petrify;

import java.lang.classfile.Annotation;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeBuilder.BlockCodeBuilder;
import java.lang.classfile.FieldBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.lang.classfile.attribute.ConstantValueAttribute;
import java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
import com.github.exabrial.petrify.compiler.model.ClassifierVine;
import com.github.exabrial.petrify.compiler.model.LeafClassEntry;
import com.github.exabrial.petrify.compiler.model.LeafTargetEntry;
import com.github.exabrial.petrify.compiler.model.ModelMetadata;
import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.compiler.model.RegressorGrove;
import com.github.exabrial.petrify.compiler.model.RegressorVine;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedCometImpact;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedTreeBranch;
import com.github.exabrial.petrify.internal.model.ByteCodeAdapter;
import com.github.exabrial.petrify.internal.model.ClassifierStratum;
import com.github.exabrial.petrify.internal.model.DoublePrecisionByteCodeAdapter;
import com.github.exabrial.petrify.internal.model.RegressorStratum;
import com.github.exabrial.petrify.internal.model.SinglePrecisionByteCodeAdapter;
import com.github.exabrial.petrify.internal.model.Stratum;
import com.github.exabrial.petrify.model.ClassifierFossil;
import com.github.exabrial.petrify.model.Fossil;
import com.github.exabrial.petrify.model.Generated;
import com.github.exabrial.petrify.model.PetrifyConstants;
import com.github.exabrial.petrify.model.RegressionFossil;

import lombok.ToString;

@ToString
public class Petrify {
	private static final Logger log = LoggerFactory.getLogger(Petrify.class);

	public static final String PETRIFIED_FOSSIL = "PetrifiedFossil$Petrify0x";
	public static final int JDK_17 = 61;

	protected static final int PREDICT_SLOT_THIS = 0;
	protected static final int PREDICT_SLOT_FEATURES = 1;
	protected static final int PREDICT_SLOT_SCORES = 2;

	protected static final int TREE_SLOT_FEATURES = 0;
	protected static final int TREE_SLOT_SCORES = 1;
	protected static final String TREE_METHOD_PREFIX = "tree_";

	protected static final String INNER_CLASS_PREFIX = "$Trees";

	protected static final int CP_ENTRIES_PER_CROSS_CLASS_INVOCATION = 5;
	public static final int DEFAULT_CONSTANT_POOL_SOFT_MAX = 55000;

	private static final ByteCodeAdapter SINGLE_PRECISION_ADAPTER = new SinglePrecisionByteCodeAdapter();
	private static final ByteCodeAdapter DOUBLE_PRECISION_ADAPTER = new DoublePrecisionByteCodeAdapter();

	private int constantPoolSoftMax = DEFAULT_CONSTANT_POOL_SOFT_MAX;

	protected static int counter = 0;

	public void setConstantPoolSoftMax(final int constantPoolSoftMax) {
		this.constantPoolSoftMax = constantPoolSoftMax;
	}

	protected int getConstantPoolSoftMax() {
		return constantPoolSoftMax;
	}

	public ClassifierFossil fossilize(final MethodHandles.Lookup lookup, final ClassifierGrove grove) {
		log.info("fossilize() compiling ClassifierGrove classes:{} precisionMode:{} summary:{} ", grove.classLabelsInt64s.length,
				grove.precisionMode, grove.summary());
		log.trace("fossilize() grove:{}", grove);
		try {
			final ClassifierStratum stratum = new ClassifierStratum(grove);

			final ClassDesc thisClass = nextClassDesc(lookup);
			final List<CompiledModel> innerClasses = new ArrayList<>();
			final byte[] fossilBytes = ClassFile.of().build(thisClass, (final ClassBuilder classBuilder) -> {
				setJdk(classBuilder);
				setClassFlags(classBuilder);
				addGeneratedAnnotation(classBuilder);
				implementFossilInterface(classBuilder, ClassifierFossil.class);
				createSerialVersionUid(classBuilder);
				createDefaultConstructor(classBuilder);
				emitMetadataMethods(classBuilder, grove.metadata);
				final ClassDesc[] treeOwners = createMethodPerEnsemble(classBuilder, stratum, thisClass, innerClasses);
				implementClassifierPredictMethod(classBuilder, stratum, treeOwners);
			});

			for (final CompiledModel innerClass : innerClasses) {
				defineInnerClass(lookup, innerClass);
			}
			final CompiledModel fossilClass = new CompiledModel(thisClass.displayName(), fossilBytes);
			final ClassifierFossil fossil = defineFossil(lookup, fossilClass, ClassifierFossil.class);
			return fossil;
		} catch (final Exception e) {
			throw new UnexpectedCometImpact(e);
		}
	}

	public RegressionFossil fossilize(final MethodHandles.Lookup lookup, final RegressorGrove grove) {
		log.info("fossilize() compiling RegressorGrove summary:{} targets:{} precisionMode:{}", grove.summary(), grove.nTargets,
				grove.precisionMode);
		log.trace("fossilize() grove:{}", grove);
		try {
			final RegressorStratum stratum = new RegressorStratum(grove);

			final ClassDesc thisClass = nextClassDesc(lookup);
			final List<CompiledModel> innerClasses = new ArrayList<>();
			final byte[] fossilBytes = ClassFile.of().build(thisClass, (final ClassBuilder classBuilder) -> {
				setJdk(classBuilder);
				setClassFlags(classBuilder);
				addGeneratedAnnotation(classBuilder);
				implementFossilInterface(classBuilder, RegressionFossil.class);
				createSerialVersionUid(classBuilder);
				createDefaultConstructor(classBuilder);
				emitMetadataMethods(classBuilder, grove.metadata);
				final ClassDesc[] treeOwners = createMethodPerEnsemble(classBuilder, stratum, thisClass, innerClasses);
				implementRegressorPredictMethod(classBuilder, stratum, treeOwners);
			});

			for (final CompiledModel innerClass : innerClasses) {
				defineInnerClass(lookup, innerClass);
			}
			final CompiledModel fossilClass = new CompiledModel(thisClass.displayName(), fossilBytes);
			final RegressionFossil fossil = defineFossil(lookup, fossilClass, RegressionFossil.class);
			return fossil;
		} catch (final Exception e) {
			throw new UnexpectedCometImpact(e);
		}
	}

	public ClassifierFossil fossilize(final MethodHandles.Lookup lookup, final ClassifierVine vine) {
		log.info("fossilize() compiling ClassifierVine classes:{} features:{} precisionMode:{}", vine.nClasses, vine.nFeatures,
				vine.precisionMode);
		log.trace("fossilize() vine:{}", vine);
		try {
			final ClassDesc thisClass = nextClassDesc(lookup);
			final byte[] fossilBytes = ClassFile.of().build(thisClass, (final ClassBuilder classBuilder) -> {
				setJdk(classBuilder);
				setClassFlags(classBuilder);
				addGeneratedAnnotation(classBuilder);
				implementFossilInterface(classBuilder, ClassifierFossil.class);
				createSerialVersionUid(classBuilder);
				createDefaultConstructor(classBuilder);
				emitMetadataMethods(classBuilder, vine.metadata);
				implementLinearClassifierPredictMethod(classBuilder, vine);
			});

			final ClassifierFossil fossil = defineFossil(lookup, new CompiledModel(thisClass.displayName(), fossilBytes),
					ClassifierFossil.class);
			return fossil;
		} catch (final Exception e) {
			throw new UnexpectedCometImpact(e);
		}
	}

	public RegressionFossil fossilize(final MethodHandles.Lookup lookup, final RegressorVine vine) {
		log.info("fossilize() compiling RegressorVine targets:{} features:{} precisionMode:{}", vine.nTargets, vine.nFeatures,
				vine.precisionMode);
		log.trace("fossilize() vine:{}", vine);
		try {
			final ClassDesc thisClass = nextClassDesc(lookup);
			final byte[] fossilBytes = ClassFile.of().build(thisClass, (final ClassBuilder classBuilder) -> {
				setJdk(classBuilder);
				setClassFlags(classBuilder);
				addGeneratedAnnotation(classBuilder);
				implementFossilInterface(classBuilder, RegressionFossil.class);
				createSerialVersionUid(classBuilder);
				createDefaultConstructor(classBuilder);
				emitMetadataMethods(classBuilder, vine.metadata);
				implementLinearRegressorPredictMethod(classBuilder, vine);
			});

			final RegressionFossil fossil = defineFossil(lookup, new CompiledModel(thisClass.displayName(), fossilBytes),
					RegressionFossil.class);
			return fossil;
		} catch (final Exception e) {
			throw new UnexpectedCometImpact(e);
		}
	}

	protected void implementLinearRegressorPredictMethod(final ClassBuilder classBuilder, final RegressorVine vine) {
		final ByteCodeAdapter adapter = getByteCodeAdapter(vine.precisionMode);
		classBuilder.withMethodBody(RegressionFossil.predict, MethodTypeDesc.of(adapter.scalarDesc(), adapter.arrayDesc()),
				ClassFile.ACC_PUBLIC, (final CodeBuilder codeBuilder) -> {
					final int nFeatures = vine.nFeatures;
					final double[] coefficients = vine.coefficients;
					final double intercept = vine.intercepts[0];

					// Push this first; it will sit beneath the accumulating score for the aggregate call
					codeBuilder.aload(PREDICT_SLOT_THIS);

					// Start accumulating: score = intercept
					adapter.ldc(codeBuilder, intercept);

					// Accumulate: score += features[f] * coefficients[f]
					for (int featureIdx = 0; featureIdx < nFeatures; featureIdx++) {
						final double coefficient = coefficients[featureIdx];
						if (coefficient != 0.0) {
							// Load features[featureIdx] onto stack
							codeBuilder.aload(PREDICT_SLOT_FEATURES);
							codeBuilder.ldc(featureIdx);
							adapter.aload(codeBuilder);

							// Multiply by coefficient and accumulate into running score
							adapter.ldc(codeBuilder, coefficient);
							adapter.mul(codeBuilder);
							adapter.add(codeBuilder);
						}
					}
					// Stack: this, score

					codeBuilder.ldc((int) vine.postTransform);

					// Invoke regressionFossil.aggregate(score, postTransform)
					codeBuilder.invokeinterface(ClassDesc.of(RegressionFossil.class.getPackageName(), RegressionFossil.class.getSimpleName()),
							RegressionFossil.aggregate, MethodTypeDesc.of(adapter.scalarDesc(), adapter.scalarDesc(), ConstantDescs.CD_byte));

					adapter.return_(codeBuilder);
				});
	}

	protected void implementLinearClassifierPredictMethod(final ClassBuilder classBuilder, final ClassifierVine vine) {
		final ByteCodeAdapter adapter = getByteCodeAdapter(vine.precisionMode);
		classBuilder.withMethodBody(ClassifierFossil.predict, MethodTypeDesc.of(ConstantDescs.CD_int, adapter.arrayDesc()),
				ClassFile.ACC_PUBLIC, (final CodeBuilder codeBuilder) -> {
					final int nClasses = vine.nClasses;
					final int nFeatures = vine.nFeatures;
					final double[] coefficients = vine.coefficients;
					final double[] intercepts = vine.intercepts;

					// Create per-class score accumulator array
					codeBuilder.ldc(nClasses);
					codeBuilder.newarray(adapter.typeKind());
					codeBuilder.astore(PREDICT_SLOT_SCORES);

					// For each class, compute: scores[c] = intercepts[c] + sum(features[f] * coefficients[c * nFeatures + f])
					for (int classIdx = 0; classIdx < nClasses; classIdx++) {
						// Push scores array ref and index for eventual store, then seed the accumulator with the intercept
						codeBuilder.aload(PREDICT_SLOT_SCORES);
						codeBuilder.ldc(classIdx);
						adapter.ldc(codeBuilder, intercepts[classIdx]);
						// Stack: scores_ref, classIdx, intercept

						// Accumulate: += features[f] * coefficients[c * nFeatures + f]
						for (int featureIdx = 0; featureIdx < nFeatures; featureIdx++) {
							final double coefficient = coefficients[classIdx * nFeatures + featureIdx];
							if (coefficient != 0.0) {
								// Load features[featureIdx] onto stack
								codeBuilder.aload(PREDICT_SLOT_FEATURES);
								codeBuilder.ldc(featureIdx);
								adapter.aload(codeBuilder);

								// Multiply by coefficient and accumulate into running score
								adapter.ldc(codeBuilder, coefficient);
								adapter.mul(codeBuilder);
								adapter.add(codeBuilder);
							}
						}
						// Stack: scores_ref, classIdx, accumulated_score

						// Store the accumulated score into scores array
						adapter.astore(codeBuilder);
					}

					// Prep and invoke fossil.classify(scores, postTransform, isBinarySingleScore)
					final boolean isBinarySingleScore = vine.isBinarySingleScore();
					codeBuilder.aload(PREDICT_SLOT_THIS);
					codeBuilder.aload(PREDICT_SLOT_SCORES);
					codeBuilder.ldc((int) vine.postTransform);
					codeBuilder.ldc(isBinarySingleScore ? 1 : 0);
					codeBuilder.invokeinterface(ClassDesc.of(ClassifierFossil.class.getPackageName(), ClassifierFossil.class.getSimpleName()),
							ClassifierFossil.classify,
							MethodTypeDesc.of(ConstantDescs.CD_int, adapter.arrayDesc(), ConstantDescs.CD_byte, ConstantDescs.CD_boolean));

					// Lookup the class array index in the classLabels
					final long[] classLabels = vine.classlabelsInts;
					emitClassLabelLookup(codeBuilder, classLabels);

					// Return the value from classLabel lookup to the callee
					codeBuilder.ireturn();
				});
	}

	protected void implementClassifierPredictMethod(final ClassBuilder classBuilder, final ClassifierStratum stratum,
			final ClassDesc[] treeOwners) {
		final ByteCodeAdapter adapter = getByteCodeAdapter(stratum.grove.precisionMode);
		final MethodTypeDesc treeMethodDesc = MethodTypeDesc.of(ConstantDescs.CD_void, adapter.arrayDesc(), adapter.arrayDesc());

		classBuilder.withMethodBody(ClassifierFossil.predict, MethodTypeDesc.of(ConstantDescs.CD_int, adapter.arrayDesc()),
				ClassFile.ACC_PUBLIC, (final CodeBuilder codeBuilder) -> {
					// Create per-class score accumulator array
					codeBuilder.ldc(stratum.classifierGrove.classLabelsInt64s.length);
					codeBuilder.newarray(adapter.typeKind());
					codeBuilder.astore(PREDICT_SLOT_SCORES);

					// Invoke each tree method
					emitTreeInvocations(codeBuilder, stratum, treeOwners, treeMethodDesc);

					// Apply per-class bias before post-transform
					emitBaseValues(codeBuilder, stratum.grove.baseValues, adapter);

					// Prep and invoke classifierFossil.classify(..)
					codeBuilder.aload(PREDICT_SLOT_THIS);
					codeBuilder.aload(PREDICT_SLOT_SCORES);
					codeBuilder.ldc((int) stratum.grove.postTransform);
					codeBuilder.ldc(stratum.isBinarySingleScore ? 1 : 0);
					codeBuilder.invokeinterface(ClassDesc.of(ClassifierFossil.class.getPackageName(), ClassifierFossil.class.getSimpleName()),
							ClassifierFossil.classify,
							MethodTypeDesc.of(ConstantDescs.CD_int, adapter.arrayDesc(), ConstantDescs.CD_byte, ConstantDescs.CD_boolean));
					// Winning class array index is now on the stack

					// Lookup the class array index in the classLabels
					final long[] classLabels = stratum.classifierGrove.classLabelsInt64s;
					emitClassLabelLookup(codeBuilder, classLabels);

					// Return the value from classLabel lookup to the callee
					codeBuilder.ireturn();
				});
	}

	protected void implementRegressorPredictMethod(final ClassBuilder classBuilder, final RegressorStratum stratum,
			final ClassDesc[] treeOwners) {
		final ByteCodeAdapter adapter = getByteCodeAdapter(stratum.grove.precisionMode);
		final MethodTypeDesc treeMethodDesc = MethodTypeDesc.of(ConstantDescs.CD_void, adapter.arrayDesc(), adapter.arrayDesc());

		classBuilder.withMethodBody(RegressionFossil.predict, MethodTypeDesc.of(adapter.scalarDesc(), adapter.arrayDesc()),
				ClassFile.ACC_PUBLIC, (final CodeBuilder codeBuilder) -> {
					// Create single-element score accumulator (single-target regression)
					codeBuilder.ldc(stratum.regressorGrove.nTargets);
					codeBuilder.newarray(adapter.typeKind());
					codeBuilder.astore(PREDICT_SLOT_SCORES);

					// Invoke each tree method
					emitTreeInvocations(codeBuilder, stratum, treeOwners, treeMethodDesc);

					// Apply base values bias
					emitBaseValues(codeBuilder, stratum.grove.baseValues, adapter);

					// Push this first, then load scores[0] for the aggregate call
					codeBuilder.aload(PREDICT_SLOT_THIS);
					codeBuilder.aload(PREDICT_SLOT_SCORES);
					codeBuilder.ldc(0);
					adapter.aload(codeBuilder);
					// Stack: this, score

					codeBuilder.ldc((int) stratum.grove.postTransform);

					// Invoke regressionFossil.aggregate(score, postTransform)
					codeBuilder.invokeinterface(ClassDesc.of(RegressionFossil.class.getPackageName(), RegressionFossil.class.getSimpleName()),
							RegressionFossil.aggregate, MethodTypeDesc.of(adapter.scalarDesc(), adapter.scalarDesc(), ConstantDescs.CD_byte));

					adapter.return_(codeBuilder);
				});
	}

	protected void emitClassifierLeaf(final CodeBuilder codeBuilder, final ClassifierStratum stratum, final int treeId,
			final int arrayIdx) {
		final ByteCodeAdapter adapter = getByteCodeAdapter(stratum.grove.precisionMode);
		final int nodeId = stratum.grove.nodesNodeIds[arrayIdx];
		final long key = PetrifyConstants.packLong(treeId, nodeId);
		for (final LeafClassEntry entry : stratum.leafClassEntries.get(key)) {
			// implement: scores[classId] += weight

			// Load scores array ref and classId index
			codeBuilder.aload(TREE_SLOT_SCORES);
			codeBuilder.ldc(entry.classId());
			// Duplicate scores ref and classId for the final store
			codeBuilder.dup2();
			// Stack: scores_ref, classId, scores_ref, classId

			// Read the current value at scores[classId]
			adapter.aload(codeBuilder);
			// Stack: scores_ref, classId, current_value

			// Grab the current leaf weight
			adapter.ldc(codeBuilder, entry.weight());
			// Add the leaf weight to the current value
			adapter.add(codeBuilder);
			// Stack: scores_ref, classId, new_value

			// Store the add result back into the array
			adapter.astore(codeBuilder);
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
		final ByteCodeAdapter adapter = getByteCodeAdapter(stratum.grove.precisionMode);
		final int nodeId = stratum.grove.nodesNodeIds[arrayIdx];
		final long key = PetrifyConstants.packLong(treeId, nodeId);
		for (final LeafTargetEntry entry : stratum.leafTargetEntries.get(key)) {
			// implement: scores[targetId] += weight

			// Load scores array ref and targetId index
			codeBuilder.aload(TREE_SLOT_SCORES);
			codeBuilder.ldc(entry.targetId());
			// Duplicate scores ref and targetId for the final store
			codeBuilder.dup2();
			// Stack: scores_ref, targetId, scores_ref, targetId

			// Read the current value at scores[targetId]
			adapter.aload(codeBuilder);
			// Stack: scores_ref, targetId, current_value

			// Grab the current leaf weight
			adapter.ldc(codeBuilder, entry.weight());
			// Add the leaf weight to the current value
			adapter.add(codeBuilder);
			// Stack: scores_ref, targetId, new_value

			// Store the add result back into the array
			adapter.astore(codeBuilder);
		}
	}

	protected ClassDesc[] createMethodPerEnsemble(final ClassBuilder classBuilder, final Stratum stratum, final ClassDesc thisClass,
			final List<CompiledModel> innerClasses) {
		final ByteCodeAdapter adapter = getByteCodeAdapter(stratum.grove.precisionMode);
		final MethodTypeDesc treeMethodDesc = MethodTypeDesc.of(ConstantDescs.CD_void, adapter.arrayDesc(), adapter.arrayDesc());
		final int[] treeRootIds = stratum.treeRootIds;
		final ClassDesc[] treeOwners = new ClassDesc[treeRootIds.length];
		final int[] treeIdxHolder = { 0 };
		// Fossil phase: pack trees onto the fossil class, reserving CP headroom for cross-class invocations
		while (treeIdxHolder[0] < treeRootIds.length && classBuilder.constantPool().size()
				+ (treeRootIds.length - treeIdxHolder[0]) * CP_ENTRIES_PER_CROSS_CLASS_INVOCATION < constantPoolSoftMax) {
			final int treeId = treeRootIds[treeIdxHolder[0]];
			final int rootArrayIdx = stratum.nodeIndex.get(PetrifyConstants.packLong(treeId, 0));
			emitTreeMethod(classBuilder, treeMethodDesc, stratum, treeId, rootArrayIdx);
			treeOwners[treeIdxHolder[0]] = thisClass;
			treeIdxHolder[0]++;
		}
		if (treeIdxHolder[0] < treeRootIds.length) {
			log.debug("createMethodPerEnsemble() fossil spill at treeIdx:{} constantPoolSize:{} remainingTrees:{}", treeIdxHolder[0],
					classBuilder.constantPool().size(), treeRootIds.length - treeIdxHolder[0]);
		}
		// Inner class phase: spill remaining trees into static inner classes
		int innerClassCounter = 0;
		while (treeIdxHolder[0] < treeRootIds.length) {
			final ClassDesc innerClassDesc = ClassDesc.of(thisClass.packageName(),
					thisClass.displayName() + INNER_CLASS_PREFIX + innerClassCounter);
			final byte[] innerBytes = ClassFile.of().build(innerClassDesc, (final ClassBuilder innerBuilder) -> {
				setJdk(innerBuilder);
				innerBuilder.withFlags(ClassFile.ACC_FINAL | ClassFile.ACC_SUPER);
				while (treeIdxHolder[0] < treeRootIds.length && innerBuilder.constantPool().size() < constantPoolSoftMax) {
					final int treeId = treeRootIds[treeIdxHolder[0]];
					final int rootArrayIdx = stratum.nodeIndex.get(PetrifyConstants.packLong(treeId, 0));
					emitTreeMethod(innerBuilder, treeMethodDesc, stratum, treeId, rootArrayIdx);
					treeOwners[treeIdxHolder[0]] = innerClassDesc;
					treeIdxHolder[0]++;
				}
			});
			innerClasses.add(new CompiledModel(innerClassDesc.displayName(), innerBytes));
			innerClassCounter++;
		}
		log.info("createMethodPerEnsemble() trees:{} innerClasses:{}", treeRootIds.length, innerClassCounter);
		return treeOwners;
	}

	protected void emitTreeInvocations(final CodeBuilder codeBuilder, final Stratum stratum, final ClassDesc[] treeOwners,
			final MethodTypeDesc treeMethodDesc) {
		for (int i = 0; i < stratum.treeRootIds.length; i++) {
			codeBuilder.aload(PREDICT_SLOT_FEATURES);
			codeBuilder.aload(PREDICT_SLOT_SCORES);
			codeBuilder.invokestatic(treeOwners[i], TREE_METHOD_PREFIX + stratum.treeRootIds[i], treeMethodDesc);
		}
	}

	protected void emitTreeMethod(final ClassBuilder classBuilder, final MethodTypeDesc treeMethodDesc, final Stratum stratum,
			final int treeId, final int rootArrayIdx) {
		// slot layout: features=0, scores=1
		classBuilder.withMethodBody(TREE_METHOD_PREFIX + treeId, treeMethodDesc, ClassFile.ACC_STATIC, (final CodeBuilder codeBuilder) -> {
			emitTree(codeBuilder, stratum, treeId, rootArrayIdx);
			codeBuilder.return_();
		});
	}

	protected void emitTree(final CodeBuilder codeBuilder, final Stratum stratum, final int treeId, final int arrayIdx) {
		final byte mode = stratum.grove.nodesModes[arrayIdx];
		switch (mode) {

			case PetrifyConstants.MODE_BRANCH_LEQ, PetrifyConstants.MODE_BRANCH_LT, PetrifyConstants.MODE_BRANCH_GEQ, PetrifyConstants.MODE_BRANCH_GT, PetrifyConstants.MODE_BRANCH_EQ, PetrifyConstants.MODE_BRANCH_NEQ -> {
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
		final ByteCodeAdapter adapter = getByteCodeAdapter(stratum.grove.precisionMode);
		final byte mode = stratum.grove.nodesModes[arrayIdx];
		final int missingTracksTrue = stratum.grove.nodesMissingValueTracksTrue[arrayIdx];

		// Equality modes cannot honor non-default missingValueTracksTrue via cmpg/cmpl alone.
		if (mode == PetrifyConstants.MODE_BRANCH_EQ && missingTracksTrue == 1) {
			// BRANCH_EQ: NaN always routes false; missingValueTracksTrue=1 would require an explicit isNaN check.
			final int nodeId = stratum.grove.nodesNodeIds[arrayIdx];
			throw new UnexpectedTreeBranch(
					"BRANCH_EQ with missingValueTracksTrue=1 is not supported. treeId:" + treeId + " nodeId:" + nodeId);
		} else if (mode == PetrifyConstants.MODE_BRANCH_NEQ && missingTracksTrue == 0) {
			// BRANCH_NEQ: NaN always routes true; missingValueTracksTrue=0 would require an explicit isNaN check.
			final int nodeId = stratum.grove.nodesNodeIds[arrayIdx];
			throw new UnexpectedTreeBranch(
					"BRANCH_NEQ with missingValueTracksTrue=0 is not supported. treeId:" + treeId + " nodeId:" + nodeId);
		} else {
			final int featureId = stratum.grove.nodesFeatureIds[arrayIdx];
			final double threshold = stratum.grove.nodesValues[arrayIdx];

			// Load features[featureId] onto stack
			codeBuilder.aload(TREE_SLOT_FEATURES);
			codeBuilder.ldc(featureId);
			adapter.aload(codeBuilder);

			// Load threshold onto stack
			adapter.ldc(codeBuilder, threshold);
			// Stack: feature, threshold

			// Compare feature to threshold; if missing/NaN, follow the "missing" policy
			final boolean invertNanPolarity = mode == PetrifyConstants.MODE_BRANCH_GT || mode == PetrifyConstants.MODE_BRANCH_GEQ;
			if (missingTracksTrue == 0 != invertNanPolarity) {
				adapter.cmpg(codeBuilder);
			} else {
				adapter.cmpl(codeBuilder);
			}
			// Stack: int {-1, 0, 1}

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

				// feature == threshold: true when stack is 0, false when -1 or 1
				case PetrifyConstants.MODE_BRANCH_EQ -> {
					// IFNE jumps when stack != 0 (stack is -1 or 1), which is the false branch
					yield Opcode.IFNE;
				}

				// feature != threshold: true when stack is -1 or 1, false when 0
				case PetrifyConstants.MODE_BRANCH_NEQ -> {
					// IFEQ jumps when stack == 0, which is the false branch
					yield Opcode.IFEQ;
				}

				default -> {
					throw new UnexpectedTreeBranch("Unknown branch mode: " + mode);
				}
			};

			final int trueNodeId = stratum.grove.nodesTrueNodeIds[arrayIdx];
			final int trueArrayIdx = stratum.nodeIndex.get(PetrifyConstants.packLong(treeId, trueNodeId));

			final int falseNodeId = stratum.grove.nodesFalseNodeIds[arrayIdx];
			final int falseArrayIdx = stratum.nodeIndex.get(PetrifyConstants.packLong(treeId, falseNodeId));

			codeBuilder.ifThenElse(branchOpcode, (final BlockCodeBuilder falseBlock) -> {
				emitTree(falseBlock, stratum, treeId, falseArrayIdx);
			}, (final BlockCodeBuilder trueBlock) -> {
				emitTree(trueBlock, stratum, treeId, trueArrayIdx);
			});
		}
	}

	protected void emitBaseValues(final CodeBuilder codeBuilder, final double[] baseValues, final ByteCodeAdapter adapter) {
		if (baseValues == null) {
			return;
		} else {
			for (int classIdx = 0; classIdx < baseValues.length; classIdx++) {
				if (baseValues[classIdx] != 0.0) {
					// implement: scores[idx] += baseValue

					// Load scores array ref and index
					codeBuilder.aload(PREDICT_SLOT_SCORES);
					codeBuilder.ldc(classIdx);
					// Duplicate scores ref and index for the final store
					codeBuilder.dup2();
					// Stack: scores_ref, classIdx, scores_ref, classIdx

					// Read the current value at scores[idx]
					adapter.aload(codeBuilder);
					// Stack: scores_ref, classIdx, current_value

					// Add the base value to the current value
					adapter.ldc(codeBuilder, baseValues[classIdx]);
					adapter.add(codeBuilder);
					// Stack: scores_ref, classIdx, new_value

					// Store the add result back into the array
					adapter.astore(codeBuilder);
				}
			}
		}
	}

	protected void emitMetadataMethods(final ClassBuilder classBuilder, final ModelMetadata metadata) {
		if (metadata != null) {
			if (metadata.modelName != null) {
				emitGetModelNameMethod(classBuilder, metadata.modelName);
			}
			if (metadata.modelVersion != null) {
				emitGetModelVersionMethod(classBuilder, metadata.modelVersion);
			}
			if (metadata.featureNames != null) {
				emitGetFeatureNamesMethod(classBuilder, metadata.featureNames);
			}
		}
	}

	protected void emitGetModelNameMethod(final ClassBuilder classBuilder, final String modelName) {
		classBuilder.withMethodBody("getModelName", MethodTypeDesc.of(ConstantDescs.CD_String), ClassFile.ACC_PUBLIC,
				(final CodeBuilder codeBuilder) -> {
					codeBuilder.ldc(modelName);
					codeBuilder.areturn();
				});
	}

	protected void emitGetModelVersionMethod(final ClassBuilder classBuilder, final String modelVersion) {
		classBuilder.withMethodBody("getModelVersion", MethodTypeDesc.of(ConstantDescs.CD_String), ClassFile.ACC_PUBLIC,
				(final CodeBuilder codeBuilder) -> {
					codeBuilder.ldc(modelVersion);
					codeBuilder.areturn();
				});
	}

	protected void emitGetFeatureNamesMethod(final ClassBuilder classBuilder, final String[] featureNames) {
		final ClassDesc listDesc = ClassDesc.of("java.util.List");
		classBuilder.withMethodBody("getFeatureNames", MethodTypeDesc.of(listDesc), ClassFile.ACC_PUBLIC,
				(final CodeBuilder codeBuilder) -> {
					codeBuilder.ldc(featureNames.length);
					codeBuilder.anewarray(ConstantDescs.CD_String);
					for (int idx = 0; idx < featureNames.length; idx++) {
						codeBuilder.dup();
						codeBuilder.ldc(idx);
						codeBuilder.ldc(featureNames[idx]);
						codeBuilder.aastore();
					}
					codeBuilder.invokestatic(listDesc, "of", MethodTypeDesc.of(listDesc, ConstantDescs.CD_Object.arrayType()), true);
					codeBuilder.areturn();
				});
	}

	protected ByteCodeAdapter getByteCodeAdapter(final PrecisionMode precisionMode) {
		final ByteCodeAdapter result = switch (precisionMode) {
			case F32:
				yield SINGLE_PRECISION_ADAPTER;
			case F64:
				yield DOUBLE_PRECISION_ADAPTER;
		};
		return result;
	}

	protected ClassDesc nextClassDesc(final MethodHandles.Lookup lookup) {
		return ClassDesc.of(lookup.lookupClass().getPackageName(), PETRIFIED_FOSSIL + Integer.toHexString(counter++));
	}

	@SuppressWarnings("unchecked")
	protected <T extends Fossil> T defineFossil(final MethodHandles.Lookup lookup, final CompiledModel compiledClass,
			final Class<T> fossilType) throws Exception {
		final Class<?> clazz = lookup.defineClass(compiledClass.classBytes());
		final T fossil = (T) clazz.getDeclaredConstructor().newInstance();
		return fossil;
	}

	protected void defineInnerClass(final MethodHandles.Lookup lookup, final CompiledModel compiledClass) throws Exception {
		lookup.defineClass(compiledClass.classBytes());
	}

	protected void setJdk(final ClassBuilder classBuilder) {
		classBuilder.withVersion(JDK_17, 0);
	}

	protected void setClassFlags(final ClassBuilder classBuilder) {
		classBuilder.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL | ClassFile.ACC_SUPER);
	}

	protected void addGeneratedAnnotation(final ClassBuilder classBuilder) {
		final ClassDesc generatedDesc = ClassDesc.of(Generated.class.getPackageName(), Generated.class.getSimpleName());
		classBuilder.with(RuntimeInvisibleAnnotationsAttribute.of(Annotation.of(generatedDesc)));
	}

	protected void implementFossilInterface(final ClassBuilder classBuilder, final Class<? extends Fossil> fossilType) {
		classBuilder.withInterfaceSymbols(ClassDesc.of(fossilType.getPackageName(), fossilType.getSimpleName()));
	}

	protected void createSerialVersionUid(final ClassBuilder classBuilder) {
		classBuilder.withField("serialVersionUID", ConstantDescs.CD_long, (final FieldBuilder fieldBuilder) -> {
			fieldBuilder.withFlags(ClassFile.ACC_PRIVATE | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL);
			fieldBuilder.with(ConstantValueAttribute.of(1L));
		});
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
