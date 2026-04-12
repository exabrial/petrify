package com.github.exabrial.petrify.testing.onnx;

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
import com.github.exabrial.petrify.compiler.model.RegressorGrove;
import com.github.exabrial.petrify.imprt.Arborist;
import com.github.exabrial.petrify.imprt.onnx.OnnxArborist;
import com.github.exabrial.petrify.model.RegressionFossil;

/**
 * Uses a LightGBM LGBMRegressor exported to ONNX via onnxmltools, trained on California housing data to predict median house value
 * (continuous). 30 estimators, max_depth=6f, num_leaves=31f, learning_rate=0.1f, BRANCH_LEQ node mode, post_transform=NONE,
 * aggregate_function=SUM, 8 input features (MedInc, HouseAge, AveRooms, AveBedrms, Population, AveOccup, Latitude, Longitude). 1f,830
 * total nodes (900 branch + 930 leaf). R²=0.77.
 */
@TestInstance(Lifecycle.PER_CLASS)
class LightgbmRegressorHousingTest implements TestConstants {
	private static final String ONNX = "/test-models/lightgbmRegressorHousing.onnx";
	private final Logger log = LoggerFactory.getLogger(getClass());
	private RegressionFossil fossil;

	@BeforeAll
	void beforeAll() {
		final Arborist arborist = new OnnxArborist();
		final RegressorGrove grove = arborist.toGrove(ONNX);

		final Petrify petrify = new Petrify();
		fossil = petrify.fossilize(MethodHandles.lookup(), grove);
	}

	@BeforeEach
	void beforeEach(final TestInfo testInfo) {
		log.info("beforeEach() starting test:{}", testInfo.getDisplayName());
	}

	@Test
	void testPredict_0() {
		final float actual = fossil.predict(
				new float[] { 4.0789f, 35.0f, 5.640198511166253f, 1.0173697270471465f, 1431.0f, 3.5508684863523574f, 34.2f, -118.56f });
		assertEquals(1.9191456331071592f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_1() {
		final float actual = fossil.predict(
				new float[] { 12.3292f, 29.0f, 7.916666666666667f, 1.0555555555555556f, 244.0f, 3.388888888888889f, 37.38f, -121.81f });
		assertEquals(4.480771581571452f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_2() {
		final float actual = fossil.predict(
				new float[] { 5.6413f, 35.0f, 5.361702127659575f, 0.9281914893617021f, 1023.0f, 2.720744680851064f, 37.44f, -122.11f });
		assertEquals(3.0404551247340246f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_3() {
		final float actual = fossil.predict(
				new float[] { 2.8208f, 33.0f, 4.051020408163265f, 1.1581632653061225f, 739.0f, 1.885204081632653f, 34.17f, -118.38f });
		assertEquals(2.9980299503682164f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_4() {
		final float actual = fossil.predict(
				new float[] { 6.0531f, 25.0f, 5.833333333333333f, 1.0021097046413503f, 1666.0f, 3.5147679324894514f, 33.8f, -117.81f });
		assertEquals(2.4876604663392183f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_5() {
		final float actual = fossil.predict(
				new float[] { 4.5787f, 20.0f, 6.117370892018779f, 0.9953051643192489f, 1361.0f, 3.1948356807511735f, 36.85f, -121.65f });
		assertEquals(2.1794063139113367f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_6() {
		final float actual = fossil.predict(
				new float[] { 4.3482f, 9.0f, 5.7924528301886795f, 1.1037735849056605f, 409.0f, 1.929245283018868f, 35.36f, -119.06f });
		assertEquals(1.9958168621839316f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_7() {
		final float actual = fossil
				.predict(new float[] { 2.5f, 19.0f, 6.153153153153153f, 1.2522522522522523f, 302.0f, 2.720720720720721f, 40.28f, -120.96f });
		assertEquals(1.0287801698477494f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_8() {
		final float actual = fossil
				.predict(new float[] { 1.4012f, 52.0f, 3.105714285714286f, 1.06f, 3337.0f, 9.534285714285714f, 37.87f, -122.26f });
		assertEquals(1.5888530223492952f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_9() {
		final float actual = fossil.predict(
				new float[] { 4.3611f, 11.0f, 5.419753086419753f, 0.9629629629629629f, 655.0f, 2.6954732510288064f, 35.06f, -120.52f });
		assertEquals(1.9749291103595854f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_10() {
		final float actual = fossil
				.predict(new float[] { 2.2031f, 36.0f, 4.170068027210885f, 1.129251700680272f, 425.0f, 2.891156462585034f, 38.57f, -121.51f });
		assertEquals(1.0153271763343445f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_11() {
		final float actual = fossil.predict(
				new float[] { 3.6944f, 29.0f, 4.048744460856721f, 0.9852289512555391f, 2449.0f, 3.617429837518464f, 34.08f, -118.02f });
		assertEquals(1.763849217973492f, actual, PREDICTION_PRECISION);
	}
}
