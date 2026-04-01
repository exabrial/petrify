package com.github.exabrial.petrify.imprt;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
import com.github.exabrial.petrify.compiler.model.LinearClassifierGrove;
import com.github.exabrial.petrify.compiler.model.RegressorGrove;
import com.github.exabrial.petrify.compiler.model.exception.MismatchedTreeSpecies;
import com.github.exabrial.petrify.compiler.model.exception.MissingSpecimen;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedCometImpact;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedPreservative;
import com.github.exabrial.petrify.model.PetrifyConstants;

import onnx.OnnxMl.AttributeProto;
import onnx.OnnxMl.GraphProto;
import onnx.OnnxMl.ModelProto;
import onnx.OnnxMl.NodeProto;

public class Arborist implements PetrifyConstants {
	protected Set<String> ML_OP_TYPES = Set.of(OP_TREE_ENSEMBLE_CLASSIFIER, OP_TREE_ENSEMBLE, OP_TREE_ENSEMBLE_REGRESSOR,
			OP_LINEAR_CLASSIFIER);
	protected Set<String> PASSTHROUGH_OP_TYPES = Set.of(OP_CAST, OP_ZIP_MAP, OP_NORMALIZER, OP_IDENTITY);

	@SuppressWarnings("unchecked")
	public <T> T toGrove(final Class<T> groveType, final String classpathLocation) {
		final ModelProto model = loadModel(classpathLocation);
		final NodeProto mlNode = findMLNode(model.getGraph());
		final String opType = mlNode.getOpType();

		final Object grove = switch (opType) {

			case OP_TREE_ENSEMBLE_CLASSIFIER, OP_TREE_ENSEMBLE -> {
				if (groveType != ClassifierGrove.class) {
					throw new MismatchedTreeSpecies("ONNX contains " + opType + " but requested: " + groveType.getSimpleName());
				} else {
					yield mapToClassifierGrove(mlNode);
				}
			}

			case OP_TREE_ENSEMBLE_REGRESSOR -> {
				if (groveType != RegressorGrove.class) {
					throw new MismatchedTreeSpecies("ONNX contains " + opType + " but requested: " + groveType.getSimpleName());
				} else {
					yield mapToRegressorGrove(mlNode);
				}
			}

			case OP_LINEAR_CLASSIFIER -> {
				if (groveType != LinearClassifierGrove.class) {
					throw new MismatchedTreeSpecies("ONNX contains " + opType + " but requested: " + groveType.getSimpleName());
				} else {
					yield mapToLinearClassifierGrove(mlNode);
				}
			}

			default -> {
				throw new UnexpectedPreservative("No mapping for ML operator: " + opType);
			}
		};
		return (T) grove;
	}

	protected ModelProto loadModel(final String classpathLocation) {
		try (final InputStream is = getClass().getResourceAsStream(classpathLocation)) {
			if (is == null) {
				throw new MissingSpecimen("ONNX model not found on classpath: " + classpathLocation);
			} else {
				return ModelProto.parseFrom(is);
			}
		} catch (final IOException e) {
			throw new UnexpectedCometImpact(e);
		}
	}

	protected NodeProto findMLNode(final GraphProto graph) {
		NodeProto mlNode = null;
		for (final NodeProto node : graph.getNodeList()) {
			final String opType = node.getOpType();
			if (ML_OP_TYPES.contains(opType)) {
				mlNode = node;
			} else if (!PASSTHROUGH_OP_TYPES.contains(opType)) {
				throw new UnexpectedPreservative("ONNX graph contains unsupported operator: " + opType);
			}
		}
		if (mlNode == null) {
			throw new UnexpectedCometImpact("No supported ML operator node found in ONNX graph");
		} else {
			return mlNode;
		}
	}

	protected ClassifierGrove mapToClassifierGrove(final NodeProto treeNode) {
		final ClassifierGrove grove = new ClassifierGrove();
		for (final AttributeProto attr : treeNode.getAttributeList()) {
			final String name = attr.getName();
			switch (name) {
				case "nodes_treeids" -> grove.setNodesTreeIds(toIntArray(attr.getIntsList()));
				case "nodes_nodeids" -> grove.setNodesNodeIds(toIntArray(attr.getIntsList()));
				case "nodes_modes" -> grove.setNodesModes(toModeBytes(attr.getStringsList()));
				case "nodes_featureids" -> grove.setNodesFeatureIds(toIntArray(attr.getIntsList()));
				case "nodes_values" -> grove.setNodesValues(toFloatArray(attr.getFloatsList()));
				case "nodes_truenodeids" -> grove.setNodesTrueNodeIds(toIntArray(attr.getIntsList()));
				case "nodes_falsenodeids" -> grove.setNodesFalseNodeIds(toIntArray(attr.getIntsList()));
				case "nodes_hitrates" -> grove.setNodesHitRates(toFloatArray(attr.getFloatsList()));
				case "nodes_missing_value_tracks_true" -> grove.setNodesMissingValueTracksTrue(toIntArray(attr.getIntsList()));
				case "class_treeids" -> grove.setClassTreeIds(toIntArray(attr.getIntsList()));
				case "class_nodeids" -> grove.setClassNodeIds(toIntArray(attr.getIntsList()));
				case "class_ids" -> grove.setClassIds(toIntArray(attr.getIntsList()));
				case "class_weights" -> grove.setClassWeights(toFloatArray(attr.getFloatsList()));
				case "classlabels_int64s" -> grove.setClassLabelsInt64s(toLongArray(attr.getIntsList()));
				case "post_transform" -> grove.setPostTransform(toPostTransform(attr.getS().toStringUtf8()));
				case "base_values" -> grove.setBaseValues(toFloatArray(attr.getFloatsList()));
				default -> {
					throw new UnexpectedPreservative("Unknown ONNX TreeEnsembleClassifier attribute: " + name);
				}
			}
		}
		return grove;
	}

	protected RegressorGrove mapToRegressorGrove(final NodeProto treeNode) {
		final RegressorGrove grove = new RegressorGrove();
		for (final AttributeProto attr : treeNode.getAttributeList()) {
			final String name = attr.getName();
			switch (name) {
				case "nodes_treeids" -> grove.setNodesTreeIds(toIntArray(attr.getIntsList()));
				case "nodes_nodeids" -> grove.setNodesNodeIds(toIntArray(attr.getIntsList()));
				case "nodes_modes" -> grove.setNodesModes(toModeBytes(attr.getStringsList()));
				case "nodes_featureids" -> grove.setNodesFeatureIds(toIntArray(attr.getIntsList()));
				case "nodes_values" -> grove.setNodesValues(toFloatArray(attr.getFloatsList()));
				case "nodes_truenodeids" -> grove.setNodesTrueNodeIds(toIntArray(attr.getIntsList()));
				case "nodes_falsenodeids" -> grove.setNodesFalseNodeIds(toIntArray(attr.getIntsList()));
				case "nodes_hitrates" -> grove.setNodesHitRates(toFloatArray(attr.getFloatsList()));
				case "nodes_missing_value_tracks_true" -> grove.setNodesMissingValueTracksTrue(toIntArray(attr.getIntsList()));
				case "target_treeids" -> grove.setTargetTreeIds(toIntArray(attr.getIntsList()));
				case "target_nodeids" -> grove.setTargetNodeIds(toIntArray(attr.getIntsList()));
				case "target_ids" -> grove.setTargetIds(toIntArray(attr.getIntsList()));
				case "target_weights" -> grove.setTargetWeights(toFloatArray(attr.getFloatsList()));
				case "n_targets" -> grove.setNTargets((int) attr.getI());
				case "post_transform" -> grove.setPostTransform(toPostTransform(attr.getS().toStringUtf8()));
				case "base_values" -> grove.setBaseValues(toFloatArray(attr.getFloatsList()));
				case "aggregate_function" -> grove.setAggregateFunction(toAggregateFunction(attr.getS().toStringUtf8()));
				default -> {
					throw new UnexpectedPreservative("Unknown ONNX TreeEnsembleRegressor attribute: " + name);
				}
			}
		}
		return grove;
	}

	protected LinearClassifierGrove mapToLinearClassifierGrove(final NodeProto mlNode) {
		final LinearClassifierGrove grove = new LinearClassifierGrove();
		for (final AttributeProto attr : mlNode.getAttributeList()) {
			final String name = attr.getName();
			switch (name) {
				case "coefficients" -> grove.setCoefficients(toFloatArray(attr.getFloatsList()));
				case "intercepts" -> grove.setIntercepts(toFloatArray(attr.getFloatsList()));
				case "classlabels_ints" -> grove.setClasslabelsInts(toLongArray(attr.getIntsList()));
				case "multi_class" -> grove.setMultiClass((int) attr.getI());
				case "post_transform" -> grove.setPostTransform(toPostTransform(attr.getS().toStringUtf8()));
				default -> {
					throw new UnexpectedPreservative("Unknown ONNX LinearClassifier attribute: " + name);
				}
			}
		}
		final int nClasses = grove.getIntercepts().length;
		final int nFeatures = grove.getCoefficients().length / nClasses;
		grove.setNClasses(nClasses);
		grove.setNFeatures(nFeatures);
		return grove;
	}

	protected byte toModeByte(final String mode) {
		return switch (mode) {
			case "LEAF" -> MODE_LEAF;
			case "BRANCH_LEQ" -> MODE_BRANCH_LEQ;
			case "BRANCH_LT" -> MODE_BRANCH_LT;
			case "BRANCH_GEQ", "BRANCH_GTE" -> MODE_BRANCH_GEQ;
			case "BRANCH_GT" -> MODE_BRANCH_GT;
			case "BRANCH_EQ" -> MODE_BRANCH_EQ;
			case "BRANCH_NEQ" -> MODE_BRANCH_NEQ;
			default -> throw new UnexpectedCometImpact("Unknown node mode: " + mode);
		};
	}

	protected byte toPostTransform(final String transform) {
		return switch (transform) {
			case "NONE" -> POST_TRANSFORM_NONE;
			case "SOFTMAX" -> POST_TRANSFORM_SOFTMAX;
			case "LOGISTIC" -> POST_TRANSFORM_LOGISTIC;
			case "SOFTMAX_ZERO" -> POST_TRANSFORM_SOFTMAX_ZERO;
			case "PROBIT" -> POST_TRANSFORM_PROBIT;
			default -> throw new UnexpectedCometImpact("Unknown post_transform: " + transform);
		};
	}

	protected byte toAggregateFunction(final String function) {
		return switch (function) {
			case "SUM" -> PetrifyConstants.AGGREGATE_SUM;
			case "AVERAGE" -> PetrifyConstants.AGGREGATE_AVERAGE;
			case "MIN" -> PetrifyConstants.AGGREGATE_MIN;
			case "MAX" -> PetrifyConstants.AGGREGATE_MAX;
			default -> throw new UnexpectedCometImpact("Unknown aggregate_function: " + function);
		};
	}

	protected int[] toIntArray(final List<Long> longs) {
		final int[] result = new int[longs.size()];
		for (int i = 0; i < longs.size(); i++) {
			result[i] = longs.get(i).intValue();
		}
		return result;
	}

	protected long[] toLongArray(final List<Long> longs) {
		final long[] result = new long[longs.size()];
		for (int i = 0; i < longs.size(); i++) {
			result[i] = longs.get(i);
		}
		return result;
	}

	protected float[] toFloatArray(final List<Float> floats) {
		final float[] result = new float[floats.size()];
		for (int i = 0; i < floats.size(); i++) {
			result[i] = floats.get(i);
		}
		return result;
	}

	protected byte[] toModeBytes(final List<com.google.protobuf.ByteString> strings) {
		final byte[] result = new byte[strings.size()];
		for (int i = 0; i < strings.size(); i++) {
			result[i] = toModeByte(strings.get(i).toStringUtf8());
		}
		return result;
	}
}
