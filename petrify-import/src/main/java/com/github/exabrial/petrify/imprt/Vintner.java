package com.github.exabrial.petrify.imprt;

import static com.github.exabrial.petrify.imprt.OnnxImportUtil.findMLNode;
import static com.github.exabrial.petrify.imprt.OnnxImportUtil.loadModel;
import static com.github.exabrial.petrify.imprt.OnnxImportUtil.toFloatArray;
import static com.github.exabrial.petrify.imprt.OnnxImportUtil.toLongArray;
import static com.github.exabrial.petrify.imprt.OnnxImportUtil.toPostTransform;

import java.util.Set;

import com.github.exabrial.petrify.compiler.model.ClassifierVine;
import com.github.exabrial.petrify.compiler.model.RegressorVine;
import com.github.exabrial.petrify.compiler.model.Vine;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedPreservative;
import com.github.exabrial.petrify.model.PetrifyConstants;

import onnx.OnnxMl.AttributeProto;
import onnx.OnnxMl.ModelProto;
import onnx.OnnxMl.NodeProto;

public class Vintner implements PetrifyConstants {
	protected static final Set<String> ML_OP_TYPES = Set.of(OP_LINEAR_CLASSIFIER, OP_LINEAR_REGRESSOR);

	@SuppressWarnings("unchecked")
	public <T extends Vine> T toVine(final String classpathLocation) {
		final ModelProto model = loadModel(getClass(), classpathLocation);
		return (T) toVine(model);
	}

	@SuppressWarnings("unchecked")
	public <T extends Vine> T toVine(final byte[] onnxBytes) {
		final ModelProto model = loadModel(onnxBytes);
		return (T) toVine(model);
	}

	@SuppressWarnings("unchecked")
	public <T extends Vine> T toVine(final ModelProto model) {
		final NodeProto mlNode = findMLNode(model.getGraph(), ML_OP_TYPES);
		final String opType = mlNode.getOpType();

		final Object vine = switch (opType) {
			case OP_LINEAR_CLASSIFIER -> mapToClassifierVine(mlNode);
			case OP_LINEAR_REGRESSOR -> mapToRegressorVine(mlNode);
			default -> throw new UnexpectedPreservative("No mapping for ML operator: " + opType);
		};
		return (T) vine;
	}

	protected ClassifierVine mapToClassifierVine(final NodeProto mlNode) {
		final ClassifierVine vine = new ClassifierVine();
		for (final AttributeProto attr : mlNode.getAttributeList()) {
			final String name = attr.getName();
			switch (name) {
				case "coefficients" -> vine.setCoefficients(toFloatArray(attr.getFloatsList()));
				case "intercepts" -> vine.setIntercepts(toFloatArray(attr.getFloatsList()));
				case "classlabels_ints" -> vine.setClasslabelsInts(toLongArray(attr.getIntsList()));
				case "multi_class" -> vine.setMultiClass((int) attr.getI());
				case "post_transform" -> vine.setPostTransform(toPostTransform(attr.getS().toStringUtf8()));
				default -> {
					throw new UnexpectedPreservative("Unknown ONNX LinearClassifier attribute: " + name);
				}
			}
		}
		final int nClasses = vine.getIntercepts().length;
		final int nFeatures = vine.getCoefficients().length / nClasses;
		vine.setNClasses(nClasses);
		vine.setNFeatures(nFeatures);
		return vine;
	}

	protected RegressorVine mapToRegressorVine(final NodeProto mlNode) {
		final RegressorVine vine = new RegressorVine();
		vine.setNTargets(1);
		vine.setPostTransform(POST_TRANSFORM_NONE);
		for (final AttributeProto attr : mlNode.getAttributeList()) {
			final String name = attr.getName();
			switch (name) {
				case "coefficients" -> vine.setCoefficients(toFloatArray(attr.getFloatsList()));
				case "intercepts" -> vine.setIntercepts(toFloatArray(attr.getFloatsList()));
				case "targets" -> vine.setNTargets((int) attr.getI());
				case "post_transform" -> vine.setPostTransform(toPostTransform(attr.getS().toStringUtf8()));
				default -> {
					throw new UnexpectedPreservative("Unknown ONNX LinearRegressor attribute: " + name);
				}
			}
		}
		final int nTargets = vine.getNTargets();
		final int nFeatures = vine.getCoefficients().length / nTargets;
		vine.setNFeatures(nFeatures);
		return vine;
	}
}
