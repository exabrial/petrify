package com.github.exabrial.petrify.imprt.onnx;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import com.github.exabrial.petrify.compiler.model.exception.MissingSpecimen;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedCometImpact;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedPreservative;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedTreeBranch;
import com.github.exabrial.petrify.model.PetrifyConstants;

import onnx.OnnxMl.GraphProto;
import onnx.OnnxMl.ModelProto;
import onnx.OnnxMl.NodeProto;

public final class OnnxImportUtil implements PetrifyConstants {
	private static final Set<String> PASSTHROUGH_OP_TYPES = Set.of(OP_CAST, OP_ZIP_MAP, OP_NORMALIZER, OP_IDENTITY);

	private OnnxImportUtil() {
	}

	public static ModelProto loadModel(final byte[] onnxBytes) {
		try {
			return ModelProto.parseFrom(onnxBytes);
		} catch (final IOException e) {
			throw new UnexpectedCometImpact(e);
		}
	}

	public static ModelProto loadModel(final Class<?> callerClass, final String classpathLocation) {
		try (final InputStream is = callerClass.getResourceAsStream(classpathLocation)) {
			if (is == null) {
				throw new MissingSpecimen("ONNX model not found on classpath: " + classpathLocation);
			} else {
				return ModelProto.parseFrom(is);
			}
		} catch (final IOException e) {
			throw new UnexpectedCometImpact(e);
		}
	}

	public static NodeProto findMLNode(final GraphProto graph, final Set<String> mlOpTypes) {
		NodeProto mlNode = null;
		for (final NodeProto node : graph.getNodeList()) {
			final String opType = node.getOpType();
			if (mlOpTypes.contains(opType)) {
				mlNode = node;
			} else if (!PASSTHROUGH_OP_TYPES.contains(opType)) {
				throw new UnexpectedPreservative("ONNX graph contains unsupported operator: " + opType);
			}
		}
		if (mlNode == null) {
			throw new UnexpectedTreeBranch("No supported ML operator node found in ONNX graph");
		} else {
			return mlNode;
		}
	}

	public static byte toPostTransform(final String transform) {
		return switch (transform) {
			case "NONE" -> POST_TRANSFORM_NONE;
			case "SOFTMAX" -> POST_TRANSFORM_SOFTMAX;
			case "LOGISTIC" -> POST_TRANSFORM_LOGISTIC;
			case "SOFTMAX_ZERO" -> POST_TRANSFORM_SOFTMAX_ZERO;
			case "PROBIT" -> POST_TRANSFORM_PROBIT;
			default -> throw new UnexpectedTreeBranch("Unknown post_transform: " + transform);
		};
	}

	public static int[] toIntArray(final List<Long> longs) {
		final int[] result = new int[longs.size()];
		for (int i = 0; i < longs.size(); i++) {
			result[i] = longs.get(i).intValue();
		}
		return result;
	}

	public static long[] toLongArray(final List<Long> longs) {
		final long[] result = new long[longs.size()];
		for (int i = 0; i < longs.size(); i++) {
			result[i] = longs.get(i);
		}
		return result;
	}

	public static double[] toDoubleArray(final List<Float> floats) {
		final double[] result = new double[floats.size()];
		for (int i = 0; i < floats.size(); i++) {
			result[i] = floats.get(i);
		}
		return result;
	}
}
