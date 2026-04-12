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
import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
import com.github.exabrial.petrify.imprt.Arborist;
import com.github.exabrial.petrify.imprt.onnx.OnnxArborist;
import com.github.exabrial.petrify.model.ClassifierFossil;

/**
 * Uses a scikit-learn RandomForestClassifier exported to ONNX via skl2onnx, trained on California housing data binned into 3 price
 * tiers (low/medium/high). 30 estimators, max_depth=6, BRANCH_LEQ node mode, post_transform=NONE (sklearn averages tree votes rather
 * than applying softmax), 8 input features (MedInc, HouseAge, AveRooms, AveBedrms, Population, AveOccup, Latitude, Longitude). 3,572
 * total nodes across 30 trees.
 */
@TestInstance(Lifecycle.PER_CLASS)
class RandomForestHousingTierTest {
	private static final String ONNX = "/test-models/randomForestHousingTier.onnx";
	private static final int LOW = 0;
	private static final int MEDIUM = 1;
	private static final int HIGH = 2;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private ClassifierFossil fossil;

	@BeforeAll
	void beforeAll() {
		final Arborist arborist = new OnnxArborist();
		final ClassifierGrove grove = arborist.toGrove(ONNX);

		final Petrify petrify = new Petrify();
		fossil = petrify.fossilize(MethodHandles.lookup(), grove);
	}

	@BeforeEach
	void beforeEach(final TestInfo testInfo) {
		log.info("beforeEach() starting test:{}", testInfo.getDisplayName());
	}

	@Test
	void testPredict_low_0() {
		final int actual = fossil.predict(new float[] { 1.5602999925613403f, 25.0f, 5.045454502105713f, 1.1333333253860474f, 845.0f,
				2.560606002807617f, 39.47999954223633f, -121.08999633789062f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_1() {
		final int actual = fossil.predict(new float[] { 2.3894999027252197f, 17.0f, 5.1732025146484375f, 1.0816993713378906f, 730.0f,
				2.3856208324432373f, 39.720001220703125f, -121.41000366210938f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_2() {
		final int actual = fossil.predict(new float[] { 2.3340001106262207f, 30.0f, 5.365217208862305f, 1.060869574546814f, 1688.0f,
				2.935652256011963f, 39.5099983215332f, -121.5199966430664f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_medium_0() {
		final int actual = fossil.predict(new float[] { 5.009099960327148f, 33.0f, 5.883771896362305f, 0.9561403393745422f, 1520.0f,
				3.3333332538604736f, 33.77000045776367f, -118.0199966430664f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_1() {
		final int actual = fossil.predict(new float[] { 4.614999771118164f, 36.0f, 5.566197395324707f, 1.0366196632385254f, 1236.0f,
				3.4816901683807373f, 33.95000076293945f, -118.04000091552734f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_2() {
		final int actual = fossil.predict(new float[] { 4.71589994430542f, 36.0f, 5.613138675689697f, 1.0364964008331299f, 498.0f,
				3.6350364685058594f, 33.88999938964844f, -118.0999984741211f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_high_0() {
		final int actual = fossil.predict(new float[] { 9.272000312805176f, 40.0f, 7.372340202331543f, 0.9503546357154846f, 720.0f,
				2.5531914234161377f, 34.040000915527344f, -118.4000015258789f });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_1() {
		final int actual = fossil.predict(new float[] { 14.286700248718262f, 49.0f, 7.603773593902588f, 0.946540892124176f, 850.0f,
				2.6729559898376465f, 34.06999969482422f, -118.45999908447266f });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_2() {
		final int actual = fossil.predict(new float[] { 11.667699813842773f, 37.0f, 7.605633735656738f, 0.9507042169570923f, 366.0f,
				2.5774648189544678f, 33.75f, -118.31999969482422f });
		assertEquals(HIGH, actual);
	}
}
