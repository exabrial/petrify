package com.github.exabrial.petrify.testing.lightgbm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.petrify.Petrify;
import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
import com.github.exabrial.petrify.imprt.Arborist;
import com.github.exabrial.petrify.imprt.lightgbm.LightGbmArborist;
import com.github.exabrial.petrify.model.ClassifierFossil;

/**
 * Uses a LightGBM native text model trained on California housing data binned into 3 price tiers (low/medium/high). 90 trees (30
 * estimators x 3 classes), max_depth=6, num_leaves=31, objective=multiclass/softmax, 8 input features (MedInc, HouseAge, AveRooms,
 * AveBedrms, Population, AveOccup, Latitude, Longitude). F64 precision mode.
 */
@TestInstance(Lifecycle.PER_CLASS)
class LightgbmHousingTierTest {
	private static final String MODEL = "/test-models/lightgbmHousingTier.txt";
	private static final int LOW = 0;
	private static final int MEDIUM = 1;
	private static final int HIGH = 2;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private ClassifierFossil fossil;

	@BeforeAll
	void beforeAll() {
		final Arborist arborist = new LightGbmArborist();
		final ClassifierGrove grove = arborist.toGrove(MODEL);

		final Petrify petrify = new Petrify();
		fossil = petrify.fossilize(MethodHandles.lookup(), grove);
	}

	@BeforeEach
	void beforeEach(final TestInfo testInfo) {
		log.info("beforeEach() starting test:{}", testInfo.getDisplayName());
	}

	@Test
	void testGetFeatureNames() {
		final List<String> featureNames = fossil.getFeatureNames();
		assertEquals(8, featureNames.size());
		assertEquals("Column_0", featureNames.get(0));
		assertEquals("Column_7", featureNames.get(7));
	}

	@Test
	void testGetModelName() {
		assertNull(fossil.getModelName());
	}

	@Test
	void testGetModelVersion() {
		assertNull(fossil.getModelVersion());
	}

	@Test
	void testPredict_low_0() {
		final int actual = fossil
				.predict(new double[] { 3.625, 16.0, 4.404833836858006, 0.8912386706948641, 912.0, 2.7552870090634443, 34.06, -117.71 });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_1() {
		final int actual = fossil
				.predict(new double[] { 2.2813, 21.0, 5.207272727272727, 1.0327272727272727, 862.0, 3.1345454545454547, 39.42, -121.71 });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_2() {
		final int actual = fossil
				.predict(new double[] { 1.3958, 52.0, 4.481481481481482, 1.0925925925925926, 188.0, 3.4814814814814814, 38.04, -121.86 });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_3() {
		final int actual = fossil
				.predict(new double[] { 2.7857, 21.0, 6.132553606237817, 1.1598440545808968, 1580.0, 3.0799220272904484, 36.35, -119.09 });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_medium_0() {
		final int actual = fossil
				.predict(new double[] { 3.055, 33.0, 3.775413711583924, 0.9598108747044918, 1888.0, 4.463356973995272, 33.87, -117.92 });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_1() {
		final int actual = fossil
				.predict(new double[] { 2.949, 17.0, 4.832365747460087, 1.0174165457184325, 3183.0, 2.3098693759071116, 32.83, -116.97 });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_2() {
		final int actual = fossil
				.predict(new double[] { 3.5192, 36.0, 5.85969387755102, 1.066326530612245, 1070.0, 2.729591836734694, 32.75, -117.04 });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_3() {
		final int actual = fossil
				.predict(new double[] { 4.1902, 42.0, 4.818604651162791, 0.9255813953488372, 656.0, 3.0511627906976746, 34.07, -118.14 });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_high_0() {
		final int actual = fossil
				.predict(new double[] { 3.5486, 35.0, 5.041388518024032, 0.9786381842456608, 1583.0, 2.1134846461949266, 34.01, -118.35 });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_1() {
		final int actual = fossil
				.predict(new double[] { 4.3906, 41.0, 4.675097276264592, 1.0038910505836576, 1001.0, 1.9474708171206225, 34.02, -118.43 });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_2() {
		final int actual = fossil
				.predict(new double[] { 3.6563, 40.0, 4.405194805194805, 0.9662337662337662, 835.0, 2.168831168831169, 34.17, -118.39 });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_3() {
		final int actual = fossil
				.predict(new double[] { 4.7381, 43.0, 6.446666666666666, 1.01, 847.0, 2.8233333333333333, 37.61, -122.41 });
		assertEquals(HIGH, actual);
	}
}
