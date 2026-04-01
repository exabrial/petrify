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
 * Uses a scikit-learn GradientBoostingClassifier (logistic/log_loss) exported to ONNX, trained on California housing data binned into
 * 3 price tiers (low/medium/high). 20 estimators x 3 classes = 60 trees, max_depth=4, learning_rate=0.1, BRANCH_LEQ node mode,
 * post_transform=SOFTMAX, 8 input features (MedInc, HouseAge, AveRooms, AveBedrms, Population, AveOccup, Latitude, Longitude).
 * Accuracy=0.77.
 */
@TestInstance(Lifecycle.PER_CLASS)
class SklearnLogisticHousingTierTest {
	private static final String ONNX = "/test-models/sklearnLogisticHousingTier.onnx";
	private static final int LOW = 0;
	private static final int MEDIUM = 1;
	private static final int HIGH = 2;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private ClassifierFossil fossil;

	@BeforeAll
	void beforeAll() {
		final Arborist arborist = new Arborist();
		final ClassifierGrove grove = arborist.toGrove(ClassifierGrove.class, ONNX);

		final Petrify petrify = new Petrify();
		fossil = petrify.fossilize(MethodHandles.lookup(), grove);
	}

	@BeforeEach
	void beforeEach(final TestInfo testInfo) {
		log.info("beforeEach() starting test:{}", testInfo.getDisplayName());
	}

	@Test
	void testPredict_low_0() {
		final int actual = fossil.predict(new float[] { 1.6812000274658203f, 25.0f, 4.192200660705566f, 1.0222841501235962f, 1392.0f,
				3.8774373531341553f, 36.060001373291016f, -119.01000213623047f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_1() {
		final int actual = fossil.predict(new float[] { 2.5f, 22.0f, 4.915999889373779f, 1.0119999647140503f, 733.0f, 2.931999921798706f,
				38.56999969482422f, -121.30999755859375f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_2() {
		final int actual = fossil.predict(new float[] { 2.0260000228881836f, 50.0f, 3.700657844543457f, 1.0592105388641357f, 616.0f,
				2.026315689086914f, 37.83000183105469f, -122.26000213623047f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_3() {
		final int actual = fossil.predict(new float[] { 1.4663000106811523f, 38.0f, 3.810725450515747f, 1.0126183032989502f, 1225.0f,
				3.8643534183502197f, 34.11000061035156f, -117.30999755859375f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_medium_0() {
		final int actual = fossil.predict(new float[] { 5.737599849700928f, 17.0f, 6.163636207580566f, 1.0202020406723022f, 1705.0f,
				3.444444417953491f, 34.279998779296875f, -118.72000122070312f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_1() {
		final int actual = fossil.predict(new float[] { 3.9205000400543213f, 43.0f, 5.407407283782959f, 1.0396825075149536f, 979.0f,
				2.589946985244751f, 37.75f, -122.48999786376953f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_2() {
		final int actual = fossil.predict(new float[] { 3.581899881362915f, 36.0f, 4.826498508453369f, 1.0757098197937012f, 703.0f,
				2.217665672302246f, 34.279998779296875f, -119.25f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_3() {
		final int actual = fossil.predict(new float[] { 3.0869998931884766f, 46.0f, 4.396588325500488f, 1.0319828987121582f, 1522.0f,
				3.2452025413513184f, 34.209999084472656f, -119.18000030517578f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_high_0() {
		final int actual = fossil.predict(new float[] { 3.48009991645813f, 52.0f, 3.9771547317504883f, 1.1858774423599243f, 1310.0f,
				1.3603322505950928f, 37.79999923706055f, -122.44000244140625f });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_1() {
		final int actual = fossil.predict(new float[] { 5.697000026702881f, 38.0f, 5.9844560623168945f, 1.0362694263458252f, 971.0f,
				2.5155439376831055f, 37.95000076293945f, -122.54000091552734f });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_2() {
		final int actual = fossil.predict(new float[] { 7.629000186920166f, 34.0f, 6.006589889526367f, 1.0049422979354858f, 1390.0f,
				2.2899506092071533f, 34.13999938964844f, -118.47000122070312f });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_3() {
		final int actual = fossil.predict(new float[] { 7.706200122833252f, 18.0f, 7.662087917327881f, 1.0384615659713745f, 937.0f,
				2.5741758346557617f, 36.810001373291016f, -119.83999633789062f });
		assertEquals(HIGH, actual);
	}
}
