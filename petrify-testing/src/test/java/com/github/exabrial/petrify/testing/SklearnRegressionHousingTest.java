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
 * Uses a scikit-learn GradientBoostingRegressor exported to ONNX, trained on California housing data to predict median house value
 * (continuous). 20 estimators, max_depth=4, learning_rate=0.1, BRANCH_LEQ node mode, post_transform=NONE, aggregate_function=SUM, 8
 * input features (MedInc, HouseAge, AveRooms, AveBedrms, Population, AveOccup, Latitude, Longitude). R²=0.68.
 */
@TestInstance(Lifecycle.PER_CLASS)
class SklearnRegressionHousingTest {
	private static final String ONNX = "/test-models/sklearnRegressionHousing.onnx";
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
		final float actual = fossil.predict(new float[] { 1.6812000274658203f, 25.0f, 4.192200660705566f, 1.0222841501235962f, 1392.0f,
				3.8774373531341553f, 36.060001373291016f, -119.01000213623047f });
		assertEquals(0.985284686088562f, actual, 0.01f);
	}

	@Test
	void testPredict_1() {
		final float actual = fossil.predict(new float[] { 2.5416998863220215f, 30.0f, 5.086021423339844f, 1.172042965888977f, 242.0f,
				2.6021504402160645f, 38.0099983215332f, -120.37000274658203f });
		assertEquals(1.246177077293396f, actual, 0.01f);
	}

	@Test
	void testPredict_2() {
		final float actual = fossil.predict(new float[] { 4.5f, 18.0f, 6.142857074737549f, 1.0729483366012573f, 912.0f, 2.772036552429199f,
				38.459999084472656f, -122.91000366210938f });
		assertEquals(2.159973621368408f, actual, 0.01f);
	}

	@Test
	void testPredict_3() {
		final float actual = fossil.predict(new float[] { 3.059799909591675f, 13.0f, 4.310055732727051f, 1.069832444190979f, 776.0f,
				2.167597770690918f, 34.040000915527344f, -117.66999816894531f });
		assertEquals(1.9039111137390137f, actual, 0.01f);
	}

	@Test
	void testPredict_4() {
		final float actual = fossil.predict(new float[] { 3.0546000003814697f, 33.0f, 2.922576427459717f, 1.02992844581604f, 3477.0f,
				2.2621991634368896f, 37.70000076293945f, -122.4800033569336f });
		assertEquals(2.6132099628448486f, actual, 0.01f);
	}

	@Test
	void testPredict_5() {
		final float actual = fossil.predict(new float[] { 2.9017999172210693f, 52.0f, 4.621212005615234f, 1.0984848737716675f, 281.0f,
				2.1287879943847656f, 37.849998474121094f, -122.27999877929688f });
		assertEquals(2.066805601119995f, actual, 0.01f);
	}

	@Test
	void testPredict_6() {
		final float actual = fossil.predict(new float[] { 3.563800096511841f, 35.0f, 4.701456546783447f, 1.065533995628357f, 1523.0f,
				3.6966018676757812f, 34.119998931884766f, -118.20999908447266f });
		assertEquals(1.806421160697937f, actual, 0.01f);
	}

	@Test
	void testPredict_7() {
		final float actual = fossil.predict(new float[] { 2.1666998863220215f, 42.0f, 5.17307710647583f, 1.0288461446762085f, 200.0f,
				1.923076868057251f, 36.959999084472656f, -122.04000091552734f });
		assertEquals(1.8960392475128174f, actual, 0.01f);
	}

	@Test
	void testPredict_8() {
		final float actual = fossil.predict(new float[] { 3.813999891281128f, 6.0f, 4.16983699798584f, 1.01902174949646f, 1541.0f,
				2.09375f, 32.91999816894531f, -117.13999938964844f });
		assertEquals(2.100045680999756f, actual, 0.01f);
	}

	@Test
	void testPredict_9() {
		final float actual = fossil.predict(new float[] { 4.173099994659424f, 36.0f, 5.491803169250488f, 0.9057376980781555f, 848.0f,
				3.475409746170044f, 34.04999923706055f, -117.93000030517578f });
		assertEquals(1.8947312831878662f, actual, 0.01f);
	}

	@Test
	void testPredict_10() {
		final float actual = fossil.predict(new float[] { 9.358599662780762f, 32.0f, 7.129353046417236f, 0.9900497794151306f, 498.0f,
				2.477612018585205f, 37.36000061035156f, -122.0999984741211f });
		assertEquals(4.411486625671387f, actual, 0.01f);
	}

	@Test
	void testPredict_11() {
		final float actual = fossil.predict(new float[] { 5.151400089263916f, 19.0f, 6.204917907714844f, 1.0016393661499023f, 2198.0f,
				3.603278636932373f, 37.11000061035156f, -121.66000366210938f });
		assertEquals(2.240135431289673f, actual, 0.01f);
	}
}
