package com.github.exabrial.petrify.imprt;

import static com.github.exabrial.petrify.imprt.OnnxImportUtil.findMLNode;
import static com.github.exabrial.petrify.imprt.OnnxImportUtil.loadModel;
import static com.github.exabrial.petrify.imprt.OnnxImportUtil.toFloatArray;
import static com.github.exabrial.petrify.imprt.OnnxImportUtil.toIntArray;
import static com.github.exabrial.petrify.imprt.OnnxImportUtil.toLongArray;
import static com.github.exabrial.petrify.imprt.OnnxImportUtil.toPostTransform;

import java.util.List;
import java.util.Set;

import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
import com.github.exabrial.petrify.compiler.model.Grove;
import com.github.exabrial.petrify.compiler.model.RegressorGrove;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedCometImpact;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedPreservative;
import com.github.exabrial.petrify.model.PetrifyConstants;

import onnx.OnnxMl.AttributeProto;
import onnx.OnnxMl.ModelProto;
import onnx.OnnxMl.NodeProto;

public class Arborist implements PetrifyConstants {
	protected static final Set<String> ML_OP_TYPES = Set.of(OP_TREE_ENSEMBLE_CLASSIFIER, OP_TREE_ENSEMBLE, OP_TREE_ENSEMBLE_REGRESSOR);

	@SuppressWarnings("unchecked")
	public <T extends Grove> T toGrove(final String classpathLocation) {
		final ModelProto model = loadModel(getClass(), classpathLocation);
		return (T) toGrove(model);
	}

	@SuppressWarnings("unchecked")
	public <T extends Grove> T toGrove(final byte[] onnxBytes) {
		final ModelProto model = loadModel(onnxBytes);
		return (T) toGrove(model);
	}

	@SuppressWarnings("unchecked")
	public <T extends Grove> T toGrove(final ModelProto model) {
		final NodeProto mlNode = findMLNode(model.getGraph(), ML_OP_TYPES);
		final String opType = mlNode.getOpType();

		final Object grove = switch (opType) {
			case OP_TREE_ENSEMBLE_CLASSIFIER, OP_TREE_ENSEMBLE -> mapToClassifierGrove(mlNode);
			case OP_TREE_ENSEMBLE_REGRESSOR -> mapToRegressorGrove(mlNode);
			default -> throw new UnexpectedPreservative("No mapping for ML operator: " + opType);
		};
		return (T) grove;
	}

	protected ClassifierGrove mapToClassifierGrove(final NodeProto treeNode) {
		final ClassifierGrove grove = new ClassifierGrove();
		for (final AttributeProto attr : treeNode.getAttributeList()) {
			final String name = attr.getName();
			switch (name) {
				case "nodes_treeids" -> grove.nodesTreeIds = toIntArray(attr.getIntsList());
				case "nodes_nodeids" -> grove.nodesNodeIds = toIntArray(attr.getIntsList());
				case "nodes_modes" -> grove.nodesModes = toModeBytes(attr.getStringsList());
				case "nodes_featureids" -> grove.nodesFeatureIds = toIntArray(attr.getIntsList());
				case "nodes_values" -> grove.nodesValues = toFloatArray(attr.getFloatsList());
				case "nodes_truenodeids" -> grove.nodesTrueNodeIds = toIntArray(attr.getIntsList());
				case "nodes_falsenodeids" -> grove.nodesFalseNodeIds = toIntArray(attr.getIntsList());
				case "nodes_hitrates" -> grove.nodesHitRates = toFloatArray(attr.getFloatsList());
				case "nodes_missing_value_tracks_true" -> grove.nodesMissingValueTracksTrue = toIntArray(attr.getIntsList());
				case "class_treeids" -> grove.classTreeIds = toIntArray(attr.getIntsList());
				case "class_nodeids" -> grove.classNodeIds = toIntArray(attr.getIntsList());
				case "class_ids" -> grove.classIds = toIntArray(attr.getIntsList());
				case "class_weights" -> grove.classWeights = toFloatArray(attr.getFloatsList());
				case "classlabels_int64s" -> grove.classLabelsInt64s = toLongArray(attr.getIntsList());
				case "post_transform" -> grove.postTransform = toPostTransform(attr.getS().toStringUtf8());
				case "base_values" -> grove.baseValues = toFloatArray(attr.getFloatsList());
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
				case "nodes_treeids" -> grove.nodesTreeIds = toIntArray(attr.getIntsList());
				case "nodes_nodeids" -> grove.nodesNodeIds = toIntArray(attr.getIntsList());
				case "nodes_modes" -> grove.nodesModes = toModeBytes(attr.getStringsList());
				case "nodes_featureids" -> grove.nodesFeatureIds = toIntArray(attr.getIntsList());
				case "nodes_values" -> grove.nodesValues = toFloatArray(attr.getFloatsList());
				case "nodes_truenodeids" -> grove.nodesTrueNodeIds = toIntArray(attr.getIntsList());
				case "nodes_falsenodeids" -> grove.nodesFalseNodeIds = toIntArray(attr.getIntsList());
				case "nodes_hitrates" -> grove.nodesHitRates = toFloatArray(attr.getFloatsList());
				case "nodes_missing_value_tracks_true" -> grove.nodesMissingValueTracksTrue = toIntArray(attr.getIntsList());
				case "target_treeids" -> grove.targetTreeIds = toIntArray(attr.getIntsList());
				case "target_nodeids" -> grove.targetNodeIds = toIntArray(attr.getIntsList());
				case "target_ids" -> grove.targetIds = toIntArray(attr.getIntsList());
				case "target_weights" -> grove.targetWeights = toFloatArray(attr.getFloatsList());
				case "n_targets" -> grove.nTargets = (int) attr.getI();
				case "post_transform" -> grove.postTransform = toPostTransform(attr.getS().toStringUtf8());
				case "base_values" -> grove.baseValues = toFloatArray(attr.getFloatsList());
				case "aggregate_function" -> grove.aggregateFunction = toAggregateFunction(attr.getS().toStringUtf8());
				default -> {
					throw new UnexpectedPreservative("Unknown ONNX TreeEnsembleRegressor attribute: " + name);
				}
			}
		}
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

	protected byte toAggregateFunction(final String function) {
		return switch (function) {
			case "SUM" -> PetrifyConstants.AGGREGATE_SUM;
			case "AVERAGE" -> PetrifyConstants.AGGREGATE_AVERAGE;
			case "MIN" -> PetrifyConstants.AGGREGATE_MIN;
			case "MAX" -> PetrifyConstants.AGGREGATE_MAX;
			default -> throw new UnexpectedCometImpact("Unknown aggregate_function: " + function);
		};
	}

	protected byte[] toModeBytes(final List<com.google.protobuf.ByteString> strings) {
		final byte[] result = new byte[strings.size()];
		for (int i = 0; i < strings.size(); i++) {
			result[i] = toModeByte(strings.get(i).toStringUtf8());
		}
		return result;
	}
}
