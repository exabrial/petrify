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
import com.github.exabrial.petrify.imprt.Vintner;
import com.github.exabrial.petrify.imprt.scikit.ScikitVintner;
import com.github.exabrial.petrify.model.ClassifierFossil;

/**
 * Uses a scikit-learn LogisticRegression (multinomial, lbfgs solver) exported to JSON via petrify-import-scikit, trained on California
 * housing data binned into 3 price tiers (low/medium/high). 8 input features (MedInc, HouseAge, AveRooms, AveBedrms, Population,
 * AveOccup, Latitude, Longitude), 3 classes, post_transform=SOFTMAX. F64 precision mode.
 */
@TestInstance(Lifecycle.PER_CLASS)
class LinearClassifierHousingTierTest {
	private static final String MODEL = "/test-models/logisticHousingTier.json";
	private static final int LOW = 0;
	private static final int MEDIUM = 1;
	private static final int HIGH = 2;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private ClassifierFossil fossil;

	@BeforeAll
	void beforeAll() {
		final Vintner vintner = new ScikitVintner();
		final ClassifierVine vine = vintner.toVine(MODEL);

		final Petrify petrify = new Petrify();
		fossil = petrify.fossilize(MethodHandles.lookup(), vine);
	}

	@BeforeEach
	void beforeEach(final TestInfo testInfo) {
		log.info("beforeEach() starting test:{}", testInfo.getDisplayName());
	}

	@Test
	void testPredict_low_0() {
		final int actual = fossil.predict(new double[] { 3.0924, 14.0, 7.762987012987013, 1.4642857142857142, 798.0,
				2.590909090909091, 37.29, -119.56 });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_1() {
		final int actual = fossil.predict(new double[] { 2.2466, 30.0, 5.277258566978193, 0.9719626168224299, 1008.0,
				3.1401869158878504, 36.64, -119.82 });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_2() {
		final int actual = fossil.predict(new double[] { 1.75, 49.0, 5.552631578947368, 1.3421052631578947, 560.0,
				3.6842105263157894, 37.81, -122.29 });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_3() {
		final int actual = fossil.predict(new double[] { 3.6389, 4.0, 4.237903225806452, 1.060483870967742, 455.0,
				1.8346774193548387, 38.27, -122.3 });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_medium_0() {
		final int actual = fossil.predict(new double[] { 3.0938, 34.0, 5.719557195571956, 1.1383763837638377, 1155.0,
				2.1309963099630997, 35.14, -120.68 });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_1() {
		final int actual = fossil.predict(new double[] { 3.5588, 37.0, 4.267515923566879, 1.0254777070063694, 621.0,
				1.9777070063694266, 37.97, -122.53 });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_2() {
		final int actual = fossil.predict(new double[] { 3.1505, 30.0, 4.266666666666667, 1.0206896551724138, 899.0,
				2.066666666666667, 33.88, -118.33 });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_3() {
		final int actual = fossil.predict(new double[] { 4.0577, 17.0, 5.87041564792176, 1.0, 1100.0, 2.6894865525672373,
				36.31, -119.33 });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_high_0() {
		final int actual = fossil.predict(new double[] { 5.5524, 52.0, 6.129032258064516, 1.0419354838709678, 1842.0,
				2.970967741935484, 34.14, -118.2 });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_1() {
		final int actual = fossil.predict(new double[] { 10.9529, 17.0, 8.609958506224066, 1.0871369294605808, 704.0,
				2.921161825726141, 32.83, -117.25 });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_2() {
		final int actual = fossil.predict(new double[] { 5.4591, 31.0, 5.981132075471698, 1.0117924528301887, 990.0,
				2.3349056603773586, 34.15, -118.48 });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_3() {
		final int actual = fossil.predict(new double[] { 10.1882, 38.0, 7.401883830455259, 1.084772370486656, 1660.0,
				2.6059654631083204, 34.04, -118.51 });
		assertEquals(HIGH, actual);
	}
}
