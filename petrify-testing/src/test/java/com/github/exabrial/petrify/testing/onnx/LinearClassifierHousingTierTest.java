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
import com.github.exabrial.petrify.compiler.model.ClassifierVine;
import com.github.exabrial.petrify.imprt.Vinter;
import com.github.exabrial.petrify.imprt.onnx.OnnxVintner;
import com.github.exabrial.petrify.model.ClassifierFossil;

/**
 * Uses a scikit-learn LogisticRegression (multinomial, lbfgs solver) exported to ONNX via skl2onnx, trained on California housing data
 * binned into 3 price tiers (low/medium/high). ONNX operator: LinearClassifier, 8 input features (MedInc, HouseAge, AveRooms,
 * AveBedrms, Population, AveOccup, Latitude, Longitude), 3 classes, post_transform=SOFTMAX.
 */
@TestInstance(Lifecycle.PER_CLASS)
class LinearClassifierHousingTierTest {
	private static final String ONNX = "/test-models/linearClassifierHousingTier.onnx";
	private static final int LOW = 0;
	private static final int MEDIUM = 1;
	private static final int HIGH = 2;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private ClassifierFossil fossil;

	@BeforeAll
	void beforeAll() {
		final Vinter vintner = new OnnxVintner();
		final ClassifierVine vine = vintner.toVine(ONNX);

		final Petrify petrify = new Petrify();
		fossil = petrify.fossilize(MethodHandles.lookup(), vine);
	}

	@BeforeEach
	void beforeEach(final TestInfo testInfo) {
		log.info("beforeEach() starting test:{}", testInfo.getDisplayName());
	}

	@Test
	void testPredict_low_0() {
		final int actual = fossil.predict(new float[] { 1.2863999605178833f, 40.0f, 2.552070379257202f, 1.1279798746109009f, 1257.0f,
				1.5771644115447998f, 33.77000045776367f, -118.19999694824219f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_1() {
		final int actual = fossil.predict(new float[] { 3.2737998962402344f, 29.0f, 6.43253231048584f, 1.1238447427749634f, 2007.0f,
				3.709796667098999f, 35.31999969482422f, -118.94999694824219f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_2() {
		final int actual = fossil.predict(new float[] { 1.875f, 41.0f, 4.400000095367432f, 1.031999945640564f, 642.0f, 5.136000156402588f,
				33.900001525878906f, -118.05000305175781f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_medium_0() {
		final int actual = fossil.predict(new float[] { 3.625f, 31.0f, 4.008431911468506f, 1.0118043422698975f, 2042.0f,
				3.443507671356201f, 33.84000015258789f, -118.08000183105469f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_1() {
		final int actual = fossil.predict(new float[] { 4.202600002288818f, 24.0f, 5.617543697357178f, 0.9894737005233765f, 731.0f,
				2.5649123191833496f, 34.59000015258789f, -120.13999938964844f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_2() {
		final int actual = fossil.predict(new float[] { 4.6184000968933105f, 36.0f, 5.760135173797607f, 1.0101351737976074f, 871.0f,
				2.9425675868988037f, 33.95000076293945f, -118.0199966430664f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_3() {
		final int actual = fossil.predict(new float[] { 3.30679988861084f, 50.0f, 4.622291088104248f, 1.00928795337677f, 1000.0f,
				3.095975160598755f, 34.18000030517578f, -118.13999938964844f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_high_1() {
		final int actual = fossil.predict(new float[] { 5.378699779510498f, 42.0f, 6.905882358551025f, 1.2901960611343384f, 480.0f,
				1.8823529481887817f, 32.849998474121094f, -117.26000213623047f });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_2() {
		final int actual = fossil.predict(new float[] { 9.070199966430664f, 43.0f, 7.335051536560059f, 1.015463948249817f, 1467.0f,
				2.520618438720703f, 34.16999816894531f, -118.18000030517578f });
		assertEquals(HIGH, actual);
	}
}
