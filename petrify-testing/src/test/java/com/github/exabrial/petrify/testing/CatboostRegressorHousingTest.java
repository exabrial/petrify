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
import com.github.exabrial.petrify.compiler.model.RegressorGrove;
import com.github.exabrial.petrify.imprt.Arborist;
import com.github.exabrial.petrify.model.RegressionFossil;

/**
 * Uses a CatBoost CatBoostRegressor exported to ONNX via CatBoost's native save_model(format="onnx"), trained on California housing
 * data to predict median house value (continuous). 30 iterations, depth=6, learning_rate=0.1, BRANCH_GT node mode (CatBoost
 * symmetric/oblivious trees), post_transform=NONE, aggregate_function=SUM, 8 input features (MedInc, HouseAge, AveRooms, AveBedrms,
 * Population, AveOccup, Latitude, Longitude). 3,810 total nodes (1,890 branch + 1,920 leaf). R²=0.72.
 */
@TestInstance(Lifecycle.PER_CLASS)
class CatboostRegressorHousingTest {
	private static final String ONNX = "/test-models/catboostRegressorHousing.onnx";
	private final Logger log = LoggerFactory.getLogger(getClass());
	private RegressionFossil fossil;

	@BeforeAll
	void beforeAll() {
		final Arborist arborist = new Arborist();
		final RegressorGrove grove = arborist.toGrove(RegressorGrove.class, ONNX);

		final Petrify petrify = new Petrify();
		fossil = petrify.fossilize(MethodHandles.lookup(), grove);
	}

	@BeforeEach
	void beforeEach(final TestInfo testInfo) {
		log.info("beforeEach() starting test:{}", testInfo.getDisplayName());
	}

	@Test
	void testPredict_0() {
		final float actual = fossil.predict(new float[] { 2.8208000659942627f, 33.0f, 4.051020622253418f, 1.15816330909729f, 739.0f,
				1.8852040767669678f, 34.16999816894531f, -118.37999725341797f });
		assertEquals(2.8243823051452637f, actual, 0.01f);
	}

	@Test
	void testPredict_1() {
		final float actual = fossil.predict(new float[] { 4.361100196838379f, 11.0f, 5.419753074645996f, 0.9629629850387573f, 655.0f,
				2.6954731941223145f, 35.060001373291016f, -120.5199966430664f });
		assertEquals(1.8920303583145142f, actual, 0.01f);
	}

	@Test
	void testPredict_2() {
		final float actual = fossil.predict(new float[] { 4.348199844360352f, 9.0f, 5.792452812194824f, 1.103773593902588f, 409.0f,
				1.9292452335357666f, 35.36000061035156f, -119.05999755859375f });
		assertEquals(2.111334800720215f, actual, 0.01f);
	}

	@Test
	void testPredict_3() {
		final float actual = fossil.predict(new float[] { 4.578700065612793f, 20.0f, 6.117371082305908f, 0.9953051805496216f, 1361.0f,
				3.194835662841797f, 36.849998474121094f, -121.6500015258789f });
		assertEquals(2.040360689163208f, actual, 0.01f);
	}

	@Test
	void testPredict_4() {
		final float actual = fossil.predict(new float[] { 2.5f, 19.0f, 6.153152942657471f, 1.252252221107483f, 302.0f, 2.7207207679748535f,
				40.279998779296875f, -120.95999908447266f });
		assertEquals(1.0152976512908936f, actual, 0.01f);
	}

	@Test
	void testPredict_5() {
		final float actual = fossil.predict(new float[] { 5.641300201416016f, 35.0f, 5.361701965332031f, 0.9281914830207825f, 1023.0f,
				2.7207446098327637f, 37.439998626708984f, -122.11000061035156f });
		assertEquals(3.043855667114258f, actual, 0.01f);
	}

	@Test
	void testPredict_6() {
		final float actual = fossil.predict(new float[] { 6.053100109100342f, 25.0f, 5.833333492279053f, 1.0021096467971802f, 1666.0f,
				3.51476788520813f, 33.79999923706055f, -117.80999755859375f });
		assertEquals(2.630784034729004f, actual, 0.01f);
	}

	@Test
	void testPredict_7() {
		final float actual = fossil.predict(new float[] { 3.6944000720977783f, 29.0f, 4.0487446784973145f, 0.985228955745697f, 2449.0f,
				3.617429733276367f, 34.08000183105469f, -118.0199966430664f });
		assertEquals(1.7913987636566162f, actual, 0.01f);
	}

	@Test
	void testPredict_8() {
		final float actual = fossil.predict(new float[] { 12.32919979095459f, 29.0f, 7.916666507720947f, 1.0555555820465088f, 244.0f,
				3.3888888359069824f, 37.380001068115234f, -121.80999755859375f });
		assertEquals(4.290546417236328f, actual, 0.01f);
	}

	@Test
	void testPredict_9() {
		final float actual = fossil.predict(new float[] { 2.2030999660491943f, 36.0f, 4.170068264007568f, 1.1292517185211182f, 425.0f,
				2.8911564350128174f, 38.56999969482422f, -121.51000213623047f });
		assertEquals(0.9178645610809326f, actual, 0.01f);
	}

	@Test
	void testPredict_10() {
		final float actual = fossil.predict(new float[] { 4.07889986038208f, 35.0f, 5.640198707580566f, 1.0173697471618652f, 1431.0f,
				3.550868511199951f, 34.20000076293945f, -118.55999755859375f });
		assertEquals(1.9590823650360107f, actual, 0.01f);
	}

	@Test
	void testPredict_11() {
		final float actual = fossil.predict(new float[] { 1.4012000560760498f, 52.0f, 3.1057143211364746f, 1.059999942779541f, 3337.0f,
				9.534285545349121f, 37.869998931884766f, -122.26000213623047f });
		assertEquals(1.3191032409667969f, actual, 0.01f);
	}
}
