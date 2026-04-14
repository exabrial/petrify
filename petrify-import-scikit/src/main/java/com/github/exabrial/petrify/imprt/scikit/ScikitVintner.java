package com.github.exabrial.petrify.imprt.scikit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.github.exabrial.petrify.compiler.model.ClassifierVine;
import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.compiler.model.RegressorVine;
import com.github.exabrial.petrify.compiler.model.Vine;
import com.github.exabrial.petrify.compiler.model.exception.MissingSpecimen;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedCometImpact;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedTreeBranch;
import com.github.exabrial.petrify.imprt.Vintner;
import com.github.exabrial.petrify.model.PetrifyConstants;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

public class ScikitVintner implements Vintner {
	protected static final String TYPE_CLASSIFIER = "classifier";
	protected static final String TYPE_REGRESSOR = "regressor";

	protected static final String POST_TRANSFORM_NONE = "none";
	protected static final String POST_TRANSFORM_SOFTMAX = "softmax";
	protected static final String POST_TRANSFORM_LOGISTIC = "logistic";
	protected static final String POST_TRANSFORM_PROBIT = "probit";

	@Override
	public <T extends Vine> T toVine(final String classpathLocation) {
		try (final InputStream is = getClass().getResourceAsStream(classpathLocation)) {
			if (is == null) {
				throw new MissingSpecimen("Scikit model not found on classpath: " + classpathLocation);
			} else {
				final byte[] bytes = is.readAllBytes();
				return toVine(bytes);
			}
		} catch (final IOException ioException) {
			throw new UnexpectedCometImpact(ioException);
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <T extends Vine> T toVine(final byte[] bytes) {
		final String json = new String(bytes, StandardCharsets.UTF_8);
		final ScikitLinearModel model;
		try (final Jsonb jsonb = JsonbBuilder.create()) {
			model = jsonb.fromJson(json, ScikitLinearModel.class);
			final Vine vine;
			switch (model.getType()) {
				case TYPE_CLASSIFIER -> {
					vine = buildClassifierVine(model);
				}
				case TYPE_REGRESSOR -> {
					vine = buildRegressorVine(model);
				}
				default -> throw new UnexpectedTreeBranch("Unsupported scikit model 'type': " + model.getType());
			}
			vine.precisionMode = PrecisionMode.F64;
			return (T) vine;
		} catch (final UnexpectedTreeBranch | UnexpectedCometImpact pe) {
			throw pe;
		} catch (final Exception exception) {
			throw new UnexpectedCometImpact(exception);
		}
	}

	protected ClassifierVine buildClassifierVine(final ScikitLinearModel model) {
		final double[][] coefficients = model.getCoefficients();
		final int nClasses = model.getClassLabels().length;
		final int nCoefficientRows = coefficients.length;
		final int nFeatures = coefficients[0].length;
		final ClassifierVine vine = new ClassifierVine();
		vine.precisionMode = PrecisionMode.F64;
		vine.nFeatures = nFeatures;
		vine.nClasses = nClasses;
		vine.classlabelsInts = model.getClassLabels();
		vine.intercepts = model.getIntercepts();
		vine.coefficients = flattenCoefficients(coefficients, nCoefficientRows, nFeatures);
		vine.postTransform = mapPostTransform(model.getPostTransform());
		vine.multiClass = nCoefficientRows == nClasses ? 1 : 0;
		return vine;
	}

	protected RegressorVine buildRegressorVine(final ScikitLinearModel model) {
		final double[][] coefficients = model.getCoefficients();
		final int nFeatures = coefficients[0].length;
		final RegressorVine vine = new RegressorVine();
		vine.precisionMode = PrecisionMode.F64;
		vine.nFeatures = nFeatures;
		vine.nTargets = coefficients.length;
		vine.intercepts = model.getIntercepts();
		vine.coefficients = flattenCoefficients(coefficients, coefficients.length, nFeatures);
		vine.postTransform = mapPostTransform(model.getPostTransform());
		return vine;
	}

	protected double[] flattenCoefficients(final double[][] coefficients, final int nRows, final int nCols) {
		final double[] flat = new double[nRows * nCols];
		for (int rowIdx = 0; rowIdx < nRows; rowIdx++) {
			System.arraycopy(coefficients[rowIdx], 0, flat, rowIdx * nCols, nCols);
		}
		return flat;
	}

	protected byte mapPostTransform(final String postTransform) {
		final byte result;
		if (postTransform == null) {
			result = PetrifyConstants.POST_TRANSFORM_NONE;
		} else {
			result = switch (postTransform) {
				case POST_TRANSFORM_NONE -> PetrifyConstants.POST_TRANSFORM_NONE;
				case POST_TRANSFORM_SOFTMAX -> PetrifyConstants.POST_TRANSFORM_SOFTMAX;
				case POST_TRANSFORM_LOGISTIC -> PetrifyConstants.POST_TRANSFORM_LOGISTIC;
				case POST_TRANSFORM_PROBIT -> PetrifyConstants.POST_TRANSFORM_PROBIT;
				default -> throw new UnexpectedTreeBranch("Unsupported 'post_transform': " + postTransform);
			};
		}
		return result;
	}
}
