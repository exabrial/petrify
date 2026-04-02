package com.github.exabrial.petrify.testing;

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
import com.github.exabrial.petrify.model.ClassifierFossil;

/**
 * Uses a scikit-learn ExtraTreesClassifier (Extremely Randomized Trees) exported to ONNX via skl2onnx, trained on California housing
 * data binned into 3 price tiers (low/medium/high). 30 estimators, max_depth=6, BRANCH_LEQ node mode, post_transform=NONE, 8 input
 * features (MedInc, HouseAge, AveRooms, AveBedrms, Population, AveOccup, Latitude, Longitude). 2,118 total nodes across 30 trees.
 * ExtraTrees differs from RandomForest in that split thresholds are chosen randomly rather than optimally, producing
 * characteristically odd threshold values and generally shallower effective trees.
 */
@TestInstance(Lifecycle.PER_CLASS)
class ExtraTreesHousingTierTest {
	private static final String ONNX = "/test-models/extraTreesHousingTier.onnx";
	private static final int LOW = 0;
	private static final int MEDIUM = 1;
	private static final int HIGH = 2;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private ClassifierFossil fossil;

	@BeforeAll
	void beforeAll() {
		final Arborist arborist = new Arborist();
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
		final int actual = fossil.predict(new float[] { 1.6368999481201172f, 39.0f, 4.911314964294434f, 1.0091743469238281f, 731.0f,
				2.235474109649658f, 40.779998779296875f, -124.16999816894531f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_1() {
		final int actual = fossil.predict(new float[] { 2.0903000831604004f, 34.0f, 7.612359523773193f, 1.4719101190567017f, 434.0f,
				2.438202142715454f, 41.790000915527344f, -120.08000183105469f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_2() {
		final int actual = fossil.predict(new float[] { 1.8242000341415405f, 37.0f, 5.273077011108398f, 1.2269231081008911f, 640.0f,
				2.461538553237915f, 40.779998779296875f, -124.19000244140625f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_medium_0() {
		final int actual = fossil.predict(new float[] { 4.635700225830078f, 4.0f, 6.0421929359436035f, 1.0447195768356323f, 10877.0f,
				2.748105049133301f, 33.150001525878906f, -117.2699966430664f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_1() {
		final int actual = fossil.predict(new float[] { 5.5696001052856445f, 17.0f, 5.984375f, 0.9479166865348816f, 702.0f, 3.65625f,
				33.150001525878906f, -117.13999938964844f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_2() {
		final int actual = fossil.predict(new float[] { 6.27869987487793f, 18.0f, 7.307128429412842f, 1.013324499130249f, 4439.0f,
				2.9573616981506348f, 32.810001373291016f, -116.98999786376953f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_high_0() {
		final int actual = fossil.predict(new float[] { 15.000100135803223f, 32.0f, 8.845041275024414f, 1.0351239442825317f, 1318.0f,
				2.7231404781341553f, 37.439998626708984f, -122.22000122070312f });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_1() {
		final int actual = fossil.predict(new float[] { 15.000100135803223f, 52.0f, 7.994475364685059f, 1.0276243686676025f, 483.0f,
				2.668508291244507f, 37.790000915527344f, -122.44000244140625f });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_2() {
		final int actual = fossil.predict(new float[] { 15.000100135803223f, 40.0f, 8.584541320800781f, 1.0f, 577.0f, 2.7874395847320557f,
				37.459999084472656f, -122.20999908447266f });
		assertEquals(HIGH, actual);
	}
}
