package com.github.exabrial.petrify.imprt.onnx;

import static com.github.exabrial.petrify.imprt.onnx.OnnxImportUtil.findMLNode;
import static com.github.exabrial.petrify.imprt.onnx.OnnxImportUtil.loadModel;
import static com.github.exabrial.petrify.imprt.onnx.OnnxImportUtil.toDoubleArray;
import static com.github.exabrial.petrify.imprt.onnx.OnnxImportUtil.toLongArray;
import static com.github.exabrial.petrify.imprt.onnx.OnnxImportUtil.toPostTransform;

import java.util.Set;

import com.github.exabrial.petrify.compiler.model.ClassifierVine;
import com.github.exabrial.petrify.compiler.model.RegressorVine;
import com.github.exabrial.petrify.compiler.model.Vine;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedPreservative;
import com.github.exabrial.petrify.imprt.Vinter;
import com.github.exabrial.petrify.model.PetrifyConstants;

import onnx.OnnxMl.AttributeProto;
import onnx.OnnxMl.ModelProto;
import onnx.OnnxMl.NodeProto;

public class OnnxVintner implements PetrifyConstants, Vinter {
	protected static final Set<String> ML_OP_TYPES = Set.of(OP_LINEAR_CLASSIFIER, OP_LINEAR_REGRESSOR);

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Vine> T toVine(final String classpathLocation) {
		final ModelProto model = loadModel(getClass(), classpathLocation);
		return (T) toVine(model);
	}

	@Override
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
				case "coefficients" -> vine.coefficients = toDoubleArray(attr.getFloatsList());
				case "intercepts" -> vine.intercepts = toDoubleArray(attr.getFloatsList());
				case "classlabels_ints" -> vine.classlabelsInts = toLongArray(attr.getIntsList());
				case "multi_class" -> vine.multiClass = (int) attr.getI();
				case "post_transform" -> vine.postTransform = toPostTransform(attr.getS().toStringUtf8());
				default -> {
					throw new UnexpectedPreservative("Unknown ONNX LinearClassifier attribute: " + name);
				}
			}
		}
		final int nClasses = vine.intercepts.length;
		final int nFeatures = vine.coefficients.length / nClasses;
		vine.nClasses = nClasses;
		vine.nFeatures = nFeatures;
		return vine;
	}

	protected RegressorVine mapToRegressorVine(final NodeProto mlNode) {
		final RegressorVine vine = new RegressorVine();
		vine.nTargets = 1;
		vine.postTransform = POST_TRANSFORM_NONE;
		for (final AttributeProto attr : mlNode.getAttributeList()) {
			final String name = attr.getName();
			switch (name) {
				case "coefficients" -> vine.coefficients = toDoubleArray(attr.getFloatsList());
				case "intercepts" -> vine.intercepts = toDoubleArray(attr.getFloatsList());
				case "targets" -> vine.nTargets = (int) attr.getI();
				case "post_transform" -> vine.postTransform = toPostTransform(attr.getS().toStringUtf8());
				default -> {
					throw new UnexpectedPreservative("Unknown ONNX LinearRegressor attribute: " + name);
				}
			}
		}
		final int nTargets = vine.nTargets;
		final int nFeatures = vine.coefficients.length / nTargets;
		vine.nFeatures = nFeatures;
		return vine;
	}
}
