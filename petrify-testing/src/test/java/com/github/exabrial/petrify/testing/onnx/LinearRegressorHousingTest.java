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
import com.github.exabrial.petrify.compiler.model.RegressorVine;
import com.github.exabrial.petrify.imprt.Vinter;
import com.github.exabrial.petrify.imprt.onnx.OnnxVintner;
import com.github.exabrial.petrify.model.RegressionFossil;

/**
 * Uses a scikit-learn LinearRegression exported to ONNX via skl2onnx, trained on California housing data (raw median house value
 * target, not binned). ONNX operator: LinearRegressor, 8 input features (MedInc, HouseAge, AveRooms, AveBedrms, Population, AveOccup,
 * Latitude, Longitude), single-target regression, post_transform=NONE.
 */
@TestInstance(Lifecycle.PER_CLASS)
class LinearRegressorHousingTest implements TestConstants {
	private static final String ONNX = "/test-models/linearRegressorHousing.onnx";
	private final Logger log = LoggerFactory.getLogger(getClass());
	private RegressionFossil fossil;

	@BeforeAll
	void beforeAll() {
		final Vinter vintner = new OnnxVintner();
		final RegressorVine vine = vintner.toVine(ONNX);

		final Petrify petrify = new Petrify();
		fossil = petrify.fossilize(MethodHandles.lookup(), vine);
	}

	@BeforeEach
	void beforeEach(final TestInfo testInfo) {
		log.info("beforeEach() starting test:{}", testInfo.getDisplayName());
	}

	@Test
	void testPredict_0() {
		final float actual = fossil.predict(new float[] { 1.6812000274658203f, 25.0f, 4.192200660705566f, 1.0222841501235962f, 1392.0f,
				3.8774373531341553f, 36.060001373291016f, -119.01000213623047f });
		assertEquals(2.0340328216552734f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_1() {
		final float actual = fossil.predict(new float[] { 2.5313000679016113f, 30.0f, 5.039383411407471f, 1.193493127822876f, 1565.0f,
				2.6797945499420166f, 35.13999938964844f, -119.45999908447266f });
		assertEquals(2.08674955368042f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_2() {
		final float actual = fossil.predict(new float[] { 3.48009991645813f, 52.0f, 3.9771547317504883f, 1.1858774423599243f, 1310.0f,
				1.3603322505950928f, 37.79999923706055f, -122.44000244140625f });
		assertEquals(2.282744884490967f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_3() {
		final float actual = fossil.predict(new float[] { 5.737599849700928f, 17.0f, 6.163636207580566f, 1.0202020406723022f, 1705.0f,
				3.444444417953491f, 34.279998779296875f, -118.72000122070312f });
		assertEquals(1.9632298946380615f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_4() {
		final float actual = fossil.predict(new float[] { 3.7249999046325684f, 34.0f, 5.492990493774414f, 1.028037428855896f, 1063.0f,
				2.483644962310791f, 36.619998931884766f, -121.93000030517578f });
		assertEquals(2.108529806137085f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_5() {
		final float actual = fossil.predict(new float[] { 4.714700222015381f, 12.0f, 5.251482963562012f, 0.9750889539718628f, 2400.0f,
				2.846975088119507f, 34.08000183105469f, -117.61000061035156f });
		assertEquals(1.9274265766143799f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_6() {
		final float actual = fossil.predict(new float[] { 5.083899974822998f, 36.0f, 6.221719264984131f, 1.0950226783752441f, 670.0f,
				3.0316741466522217f, 33.88999938964844f, -118.0199966430664f });
		assertEquals(2.143357515335083f, actual, PREDICTION_PRECISION);
	}

	@Test
	void testPredict_7() {
		final float actual = fossil.predict(new float[] { 3.6907999515533447f, 38.0f, 4.962825298309326f, 1.048327088356018f, 1011.0f,
				3.758364200592041f, 33.91999816894531f, -118.08000183105469f });
		assertEquals(2.1649575233459473f, actual, PREDICTION_PRECISION);
	}
}
