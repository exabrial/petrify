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
 * Uses a real XGBoost-exported ONNX model with 60 trees (20 estimators x 3 classes), max_depth=5, BRANCH_LT node mode,
 * post_transform=SOFTMAX, and multi-class encoding (class_ids contains 0, 1, 2). 8 input features, 3 output classes.
 */
@TestInstance(Lifecycle.PER_CLASS)
class XgboostMulticlassTest {
	private static final String ONNX = "/test-models/xgboostMulticlass.onnx";
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
		final int actual = fossil.predict(new float[] { -0.678752064704895f, -0.5693612098693848f, 1.2568374872207642f,
				0.3749561905860901f, 0.21228483319282532f, -0.6296670436859131f, 1.4676686525344849f, -1.6368337869644165f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_1() {
		final int actual = fossil.predict(new float[] { -0.8830124735832214f, 0.417447030544281f, 5.079479694366455f, 4.120763778686523f,
				0.8407363295555115f, -4.024209499359131f, 2.851529121398926f, -0.5642896294593811f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_2() {
		final int actual = fossil.predict(new float[] { -1.8626865148544312f, 0.8381772041320801f, 0.19191764295101166f,
				-0.004864285700023174f, 0.6885210275650024f, 0.19517794251441956f, -0.8613541722297668f, -3.0640668869018555f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_low_3() {
		final int actual = fossil.predict(new float[] { 0.9065054059028625f, 0.5063867568969727f, 2.669450521469116f, 2.9384872913360596f,
				1.2009934186935425f, -2.5835533142089844f, 3.6421966552734375f, 1.7998372316360474f });
		assertEquals(LOW, actual);
	}

	@Test
	void testPredict_medium_0() {
		final int actual = fossil.predict(new float[] { 0.8590425252914429f, -0.36663389205932617f, -5.430967330932617f,
				-0.8839991092681885f, 3.1817734241485596f, 2.29266095161438f, -2.2941150665283203f, 2.5335352420806885f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_1() {
		final int actual = fossil.predict(new float[] { 1.8307342529296875f, 1.4381955862045288f, 0.7252960205078125f, 1.0435115098953247f,
				-1.9806571006774902f, 0.1484915018081665f, -1.3663058280944824f, -1.177578091621399f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_2() {
		final int actual = fossil.predict(new float[] { 0.9655757546424866f, 0.3131430149078369f, 0.901984453201294f, -0.7028490304946899f,
				-1.847639560699463f, -0.34268781542778015f, 0.8560887575149536f, -2.268529176712036f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_medium_3() {
		final int actual = fossil.predict(new float[] { -0.9849361777305603f, -0.22878433763980865f, -0.865989625453949f,
				2.3769474029541016f, 2.0108346939086914f, 1.534509301185608f, -2.5829660892486572f, 2.9382095336914062f });
		assertEquals(MEDIUM, actual);
	}

	@Test
	void testPredict_high_0() {
		final int actual = fossil.predict(new float[] { -2.817246675491333f, 1.5279815196990967f, 6.153116226196289f, 1.3926198482513428f,
				-3.5692412853240967f, -4.341825485229492f, -2.5569562911987305f, -1.3390769958496094f });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_1() {
		final int actual = fossil.predict(new float[] { 0.7860963940620422f, -1.2486634254455566f, -1.0436816215515137f,
				-1.547210454940796f, -0.0364387184381485f, -2.349612236022949f, -0.6311526298522949f, -1.9608840942382812f });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_2() {
		final int actual = fossil.predict(new float[] { 1.2039819955825806f, 0.5358569622039795f, 2.6307055950164795f, 1.6342179775238037f,
				-2.082016706466675f, 0.056927867233753204f, 1.2588229179382324f, -2.2892467975616455f });
		assertEquals(HIGH, actual);
	}

	@Test
	void testPredict_high_3() {
		final int actual = fossil.predict(new float[] { -1.3500069379806519f, -0.22836078703403473f, 3.781646728515625f,
				0.7583394050598145f, -2.3383982181549072f, -3.7734954357147217f, -1.9983478784561157f, 0.621875524520874f });
		assertEquals(HIGH, actual);
	}
}
