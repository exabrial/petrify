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
 * Uses a real XGBoost-exported ONNX model with 3 trees (n_estimators=3), BRANCH_LT node mode, post_transform=LOGISTIC, and
 * sklearn-style binary encoding (class_ids all 0).
 */
@TestInstance(Lifecycle.PER_CLASS)
class XgboostSimpleTest {
	private static final String ONNX = "/test-models/xgboostSimple.onnx";
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
	void testPredict_1_0() {
		final int actual = fossil.predict(new float[] { 1.0f, 2.0f, 3.0f, 4.0f });
		assertEquals(1, actual);
	}

	@Test
	void testPredict_1_1() {
		final int actual = fossil.predict(new float[] { 9.0f, 9.0f, 9.0f, 9.0f });
		assertEquals(1, actual);
	}

	@Test
	void testPredict_0_0() {
		final int actual = fossil.predict(new float[] { 0.0f, 0.0f, 0.0f, 0.0f });
		assertEquals(0, actual);
	}

	@Test
	void testPredict_0_1() {
		final int actual = fossil.predict(new float[] { -1.0f, -2.0f, -1.0f, -2.0f });
		assertEquals(0, actual);
	}

	@Test
	void testPredict_0_2() {
		final int actual = fossil.predict(new float[] { 3.0f, 0.0f, 0.0f, 0.1f });
		assertEquals(0, actual);
	}

	@Test
	void testPredict_1_2() {
		final int actual = fossil.predict(new float[] { 3.0f, 0.0f, 0.0f, 0.3f });
		assertEquals(1, actual);
	}
}
