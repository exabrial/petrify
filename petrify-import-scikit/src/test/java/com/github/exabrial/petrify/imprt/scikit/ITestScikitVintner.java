package com.github.exabrial.petrify.imprt.scikit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.compiler.model.ClassifierVine;
import com.github.exabrial.petrify.compiler.model.PreTransformer;
import com.github.exabrial.petrify.compiler.model.PreTransformerType;
import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.compiler.model.RegressorVine;
import com.github.exabrial.petrify.model.PetrifyConstants;

class ITestScikitVintner {
	private static final String CLASSIFIER_MODEL = "/models/linearClassifierPreTransformers.json";
	private static final String REGRESSOR_MODEL = "/models/linearRegressorPreTransformers.json";
	private static final double[] COEFFICIENTS = { 0.4d, 0.01d, -0.03d };
	private static final double[] INTERCEPTS = { -0.5d };
	private static final String[] FEATURE_NAMES = { "MedInc", "HouseAge", "Population" };
	private static final PreTransformer[][] PRE_TRANSFORMERS = { { new PreTransformer(PreTransformerType.noop, 0.0d) },
			{ new PreTransformer(PreTransformerType.noop, 0.0d) },
			{ new PreTransformer(PreTransformerType.fill, 1000.0d), new PreTransformer(PreTransformerType.max, 20000.0d),
					new PreTransformer(PreTransformerType.min, 100.0d), new PreTransformer(PreTransformerType.log1p, 0.0d) } };

	@Test
	void testToVine_classifier() {
		final ScikitVintner vintner = new ScikitVintner();
		final ClassifierVine vine = vintner.toVine(CLASSIFIER_MODEL);
		assertEquals(PrecisionMode.F64, vine.precisionMode);
		assertEquals(PetrifyConstants.POST_TRANSFORM_LOGISTIC, vine.postTransform);
		assertEquals(3, vine.nFeatures);
		assertEquals(2, vine.nClasses);
		assertEquals(0, vine.multiClass);
		assertArrayEquals(new long[] { 0L, 1L }, vine.classlabelsInts);
		assertArrayEquals(COEFFICIENTS, vine.coefficients);
		assertArrayEquals(INTERCEPTS, vine.intercepts);
		assertArrayEquals(PRE_TRANSFORMERS, vine.preTransformers);
		assertArrayEquals(FEATURE_NAMES, vine.metadata.featureNames);
		assertEquals("linear", vine.metadata.modelFamily);
		assertEquals("linear_classifier_pre_transformers", vine.metadata.modelName);
		assertEquals(1, vine.metadata.majorVersion);
		assertEquals(0, vine.metadata.minorVersion);
	}

	@Test
	void testToVine_regressor() {
		final ScikitVintner vintner = new ScikitVintner();
		final RegressorVine vine = vintner.toVine(REGRESSOR_MODEL);
		assertEquals(PrecisionMode.F64, vine.precisionMode);
		assertEquals(PetrifyConstants.POST_TRANSFORM_LOGISTIC, vine.postTransform);
		assertEquals(3, vine.nFeatures);
		assertEquals(1, vine.nTargets);
		assertArrayEquals(COEFFICIENTS, vine.coefficients);
		assertArrayEquals(INTERCEPTS, vine.intercepts);
		assertArrayEquals(PRE_TRANSFORMERS, vine.preTransformers);
		assertArrayEquals(FEATURE_NAMES, vine.metadata.featureNames);
		assertEquals("linear", vine.metadata.modelFamily);
		assertEquals("linear_regressor_pre_transformers", vine.metadata.modelName);
		assertEquals(1, vine.metadata.majorVersion);
		assertEquals(0, vine.metadata.minorVersion);
	}
}
