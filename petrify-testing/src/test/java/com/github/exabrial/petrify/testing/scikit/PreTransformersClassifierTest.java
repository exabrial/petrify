package com.github.exabrial.petrify.testing.scikit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.petrify.Petrify;
import com.github.exabrial.petrify.compiler.model.ClassifierVine;
import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.imprt.Vintner;
import com.github.exabrial.petrify.imprt.scikit.ScikitVintner;
import com.github.exabrial.petrify.model.ClassifierFossil;

/**
 * Scikit JSON binary LogisticRegression with pre-transformers, post_transform=LOGISTIC, class labels 0 and 1. Features: MedInc (no
 * transformers), HouseAge ({@code noop}), Population ({@code fill} 1000, {@code max} 20000, {@code min} 100, {@code log1p}). Expected
 * labels computed independently in Python. The {@code fill} case predicts 1 only because {@code fill} replaces the NaN; an
 * untransformed NaN score would predict 0. Compiled at both F64 and F32.
 */
@TestInstance(Lifecycle.PER_CLASS)
class PreTransformersClassifierTest {
	private static final String MODEL = "/test-models/preTransformersClassifier.json";
	private final Logger log = LoggerFactory.getLogger(getClass());
	private ClassifierFossil fossilF64;
	private ClassifierFossil fossilF32;

	@BeforeAll
	void beforeAll() {
		final Vintner vintner = new ScikitVintner();
		final Petrify petrify = new Petrify();
		final ClassifierVine vineF64 = vintner.toVine(MODEL);
		fossilF64 = petrify.fossilize(MethodHandles.lookup(), vineF64);
		final ClassifierVine vineF32 = vintner.toVine(MODEL);
		vineF32.precisionMode = PrecisionMode.F32;
		fossilF32 = petrify.fossilize(MethodHandles.lookup(), vineF32);
	}

	@BeforeEach
	void beforeEach(final TestInfo testInfo) {
		log.info("beforeEach() starting test:{}", testInfo.getDisplayName());
	}

	@Test
	void testPredict_fill_f64() {
		final int actual = fossilF64.predict(new double[] { 3.0d, 20.0d, Double.NaN });
		assertEquals(1, actual);
	}

	@Test
	void testPredict_max_f64() {
		final int actual = fossilF64.predict(new double[] { 3.0d, 20.0d, 50000.0d });
		assertEquals(1, actual);
	}

	@Test
	void testPredict_min_f64() {
		final int actual = fossilF64.predict(new double[] { 3.0d, 20.0d, 10.0d });
		assertEquals(0, actual);
	}

	@Test
	void testPredict_fill_f32() {
		final int actual = fossilF32.predict(new float[] { 3.0f, 20.0f, Float.NaN });
		assertEquals(1, actual);
	}

	@Test
	void testPredict_max_f32() {
		final int actual = fossilF32.predict(new float[] { 3.0f, 20.0f, 50000.0f });
		assertEquals(1, actual);
	}

	@Test
	void testPredict_min_f32() {
		final int actual = fossilF32.predict(new float[] { 3.0f, 20.0f, 10.0f });
		assertEquals(0, actual);
	}
}
