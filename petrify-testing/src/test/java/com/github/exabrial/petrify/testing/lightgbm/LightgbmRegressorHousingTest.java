package com.github.exabrial.petrify.testing.lightgbm;

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
import com.github.exabrial.petrify.imprt.lightgbm.LightGbmArborist;
import com.github.exabrial.petrify.model.RegressionFossil;

/**
 * Uses a LightGBM native text model trained on California housing data to predict median house value (continuous). 30 estimators,
 * max_depth=6, num_leaves=31, learning_rate=0.1, objective=regression, 8 input features (MedInc, HouseAge, AveRooms, AveBedrms,
 * Population, AveOccup, Latitude, Longitude). F64 precision mode.
 */
@TestInstance(Lifecycle.PER_CLASS)
class LightgbmRegressorHousingTest {
	private static final String MODEL = "/test-models/lightgbmRegressorHousing.txt";
	private final Logger log = LoggerFactory.getLogger(getClass());
	private RegressionFossil fossil;

	@BeforeAll
	void beforeAll() {
		final Arborist arborist = new LightGbmArborist();
		final RegressorGrove grove = arborist.toGrove(MODEL);

		final Petrify petrify = new Petrify();
		fossil = petrify.fossilize(MethodHandles.lookup(), grove);
	}

	@BeforeEach
	void beforeEach(final TestInfo testInfo) {
		log.info("beforeEach() starting test:{}", testInfo.getDisplayName());
	}

	@Test
	void testPredict_0() {
		final double actual = fossil
				.predict(new double[] { 4.0789, 35.0, 5.640198511166253, 1.0173697270471465, 1431.0, 3.5508684863523574, 34.2, -118.56 });
		assertEquals(1.9191456331071592d, actual);
	}

	@Test
	void testPredict_1() {
		final double actual = fossil
				.predict(new double[] { 12.3292, 29.0, 7.916666666666667, 1.0555555555555556, 244.0, 3.388888888888889, 37.38, -121.81 });
		assertEquals(4.480771581571452d, actual);
	}

	@Test
	void testPredict_2() {
		final double actual = fossil
				.predict(new double[] { 5.6413, 35.0, 5.361702127659575, 0.9281914893617021, 1023.0, 2.720744680851064, 37.44, -122.11 });
		assertEquals(3.0404551247340246d, actual);
	}

	@Test
	void testPredict_3() {
		final double actual = fossil
				.predict(new double[] { 2.8208, 33.0, 4.051020408163265, 1.1581632653061225, 739.0, 1.885204081632653, 34.17, -118.38 });
		assertEquals(2.9980299503682164d, actual);
	}

	@Test
	void testPredict_4() {
		final double actual = fossil
				.predict(new double[] { 6.0531, 25.0, 5.833333333333333, 1.0021097046413503, 1666.0, 3.5147679324894514, 33.8, -117.81 });
		assertEquals(2.4876604663392183d, actual);
	}

	@Test
	void testPredict_5() {
		final double actual = fossil
				.predict(new double[] { 4.5787, 20.0, 6.117370892018779, 0.9953051643192489, 1361.0, 3.1948356807511735, 36.85, -121.65 });
		assertEquals(2.1794063139113367d, actual);
	}

	@Test
	void testPredict_6() {
		final double actual = fossil
				.predict(new double[] { 4.3482, 9.0, 5.7924528301886795, 1.1037735849056605, 409.0, 1.929245283018868, 35.36, -119.06 });
		assertEquals(1.9958168621839316d, actual);
	}

	@Test
	void testPredict_7() {
		final double actual = fossil
				.predict(new double[] { 2.5, 19.0, 6.153153153153153, 1.2522522522522523, 302.0, 2.720720720720721, 40.28, -120.96 });
		assertEquals(1.0287801698477494d, actual);
	}

	@Test
	void testPredict_8() {
		final double actual = fossil
				.predict(new double[] { 1.4012, 52.0, 3.105714285714286, 1.06, 3337.0, 9.534285714285714, 37.87, -122.26 });
		assertEquals(1.5888530223492952d, actual);
	}

	@Test
	void testPredict_9() {
		final double actual = fossil
				.predict(new double[] { 4.3611, 11.0, 5.419753086419753, 0.9629629629629629, 655.0, 2.6954732510288064, 35.06, -120.52 });
		assertEquals(1.9749291103595854d, actual);
	}

	@Test
	void testPredict_10() {
		final double actual = fossil
				.predict(new double[] { 2.2031, 36.0, 4.170068027210885, 1.129251700680272, 425.0, 2.891156462585034, 38.57, -121.51 });
		assertEquals(1.0153271763343445d, actual);
	}

	@Test
	void testPredict_11() {
		final double actual = fossil
				.predict(new double[] { 3.6944, 29.0, 4.048744460856721, 0.9852289512555391, 2449.0, 3.617429837518464, 34.08, -118.02 });
		assertEquals(1.763849217973492d, actual);
	}
}
