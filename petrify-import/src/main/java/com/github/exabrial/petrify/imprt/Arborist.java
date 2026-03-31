package com.github.exabrial.petrify.imprt;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import com.github.exabrial.petrify.compiler.model.Grove;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedCometImpact;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedPreservative;
import com.github.exabrial.petrify.model.PetrifyConstants;

import onnx.OnnxMl.AttributeProto;
import onnx.OnnxMl.GraphProto;
import onnx.OnnxMl.ModelProto;
import onnx.OnnxMl.NodeProto;

public class Arborist implements PetrifyConstants {
	protected static final Set<String> KNOWN_OP_TYPES = Set.of(
			OP_TREE_ENSEMBLE_CLASSIFIER, OP_TREE_ENSEMBLE, OP_CAST, OP_ZIP_MAP, OP_IDENTITY);

	public Grove toGrove(final String classpathLocation) {
		final ModelProto model = loadModel(classpathLocation);
		final NodeProto treeNode = findTreeEnsembleNode(model.getGraph());
		return mapToGrove(treeNode);
	}

	protected ModelProto loadModel(final String classpathLocation) {
		try (final InputStream is = getClass().getResourceAsStream(classpathLocation)) {
			if (is == null) {
				throw new UnexpectedCometImpact("ONNX model not found on classpath: " + classpathLocation);
			}
			return ModelProto.parseFrom(is);
		} catch (final IOException e) {
			throw new UnexpectedCometImpact(e);
		}
	}

	protected NodeProto findTreeEnsembleNode(final GraphProto graph) {
		NodeProto treeNode = null;
		for (final NodeProto node : graph.getNodeList()) {
			final String opType = node.getOpType();
			if (OP_TREE_ENSEMBLE_CLASSIFIER.equals(opType) || OP_TREE_ENSEMBLE.equals(opType)) {
				treeNode = node;
			} else if (!KNOWN_OP_TYPES.contains(opType)) {
				throw new UnexpectedPreservative("ONNX graph contains unsupported operator: " + opType);
			}
		}
		if (treeNode == null) {
			throw new UnexpectedCometImpact("No TreeEnsembleClassifier or TreeEnsemble node found in ONNX graph");
		} else {
			return treeNode;
		}
	}

	protected Grove mapToGrove(final NodeProto treeNode) {
		final Grove grove = new Grove();
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
					throw new UnexpectedPreservative("Unknown ONNX TreeEnsemble attribute: " + name);
				}
			}
		}
		return grove;
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

	protected byte toModeByte(final String mode) {
		return switch (mode) {
			case "LEAF" -> MODE_LEAF;
			case "BRANCH_LEQ" -> MODE_BRANCH_LEQ;
			case "BRANCH_LT" -> MODE_BRANCH_LT;
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
}
