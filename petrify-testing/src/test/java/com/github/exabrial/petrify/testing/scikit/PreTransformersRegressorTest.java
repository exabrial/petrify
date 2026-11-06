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
import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.compiler.model.RegressorVine;
import com.github.exabrial.petrify.imprt.Vintner;
import com.github.exabrial.petrify.imprt.scikit.ScikitVintner;
import com.github.exabrial.petrify.model.RegressionFossil;

/**
 * Scikit JSON LinearRegression with pre-transformers, post_transform=NONE. Features: MedInc (no transformers), HouseAge
 * ({@code noop}), Population ({@code fill} 1000, {@code max} 20000, {@code min} 100, {@code log1p}). Expected values computed
 * independently in Python ({@code math.log1p} for F64, {@code numpy.float32} for F32). Compiled at both F64 and F32.
 */
@TestInstance(Lifecycle.PER_CLASS)
class PreTransformersRegressorTest {
	private static final String MODEL = "/test-models/preTransformersRegressor.json";
	private static final double F64_PRECISION = 0.000000000001d;
	private static final float F32_PRECISION = 0.00001f;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private RegressionFossil fossilF64;
	private RegressionFossil fossilF32;

	@BeforeAll
	void beforeAll() {
		final Vintner vintner = new ScikitVintner();
		final Petrify petrify = new Petrify();
		final RegressorVine vineF64 = vintner.toVine(MODEL);
		fossilF64 = petrify.fossilize(MethodHandles.lookup(), vineF64);
		final RegressorVine vineF32 = vintner.toVine(MODEL);
		vineF32.precisionMode = PrecisionMode.F32;
		fossilF32 = petrify.fossilize(MethodHandles.lookup(), vineF32);
	}

	@BeforeEach
	void beforeEach(final TestInfo testInfo) {
		log.info("beforeEach() starting test:{}", testInfo.getDisplayName());
	}

	@Test
	void testPredict_fill_f64() {
		final double actual = fossilF64.predict(new double[] { 3.0d, 20.0d, Double.NaN });
		assertEquals(2.9726264337945665d, actual, F64_PRECISION);
	}

	@Test
	void testPredict_max_f64() {
		final double actual = fossilF64.predict(new double[] { 3.0d, 20.0d, 50000.0d });
		assertEquals(3.871061265385851d, actual, F64_PRECISION);
	}

	@Test
	void testPredict_min_f64() {
		final double actual = fossilF64.predict(new double[] { 3.0d, 20.0d, 10.0d });
		assertEquals(2.284536155052378d, actual, F64_PRECISION);
	}

	@Test
	void testPredict_log1p_f64() {
		final double actual = fossilF64.predict(new double[] { 3.0d, 20.0d, 500.0d });
		assertEquals(2.7649818303254596d, actual, F64_PRECISION);
	}

	@Test
	void testPredict_noop_f64() {
		final double actual = fossilF64.predict(new double[] { 3.0d, 40.0d, 500.0d });
		assertEquals(2.9649818303254594d, actual, F64_PRECISION);
	}

	@Test
	void testPredict_fill_f32() {
		final float actual = fossilF32.predict(new float[] { 3.0f, 20.0f, Float.NaN });
		assertEquals(2.9726266860961914f, actual, F32_PRECISION);
	}

	@Test
	void testPredict_max_f32() {
		final float actual = fossilF32.predict(new float[] { 3.0f, 20.0f, 50000.0f });
		assertEquals(3.8710615634918213f, actual, F32_PRECISION);
	}

	@Test
	void testPredict_min_f32() {
		final float actual = fossilF32.predict(new float[] { 3.0f, 20.0f, 10.0f });
		assertEquals(2.284536123275757f, actual, F32_PRECISION);
	}

	@Test
	void testPredict_log1p_f32() {
		final float actual = fossilF32.predict(new float[] { 3.0f, 20.0f, 500.0f });
		assertEquals(2.764981985092163f, actual, F32_PRECISION);
	}

	@Test
	void testPredict_noop_f32() {
		final float actual = fossilF32.predict(new float[] { 3.0f, 40.0f, 500.0f });
		assertEquals(2.964982032775879f, actual, F32_PRECISION);
	}
}
