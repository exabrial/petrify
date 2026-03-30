package com.github.exabrial.petrify.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.Petrify;
import com.github.exabrial.petrify.imprt.Arborist;
import com.github.exabrial.petrify.model.Fossil;
import com.github.exabrial.petrify.model.Grove;

/**
 * Uses a real XGBoost-exported ONNX model with 3 trees (n_estimators=3), BRANCH_LT node mode,
 * post_transform=LOGISTIC, and sklearn-style binary encoding (class_ids all 0). This test
 * will fail until Petrify supports multi-tree ensemble weight accumulation and post_transform.
 */
class XgboostSimpleTest {

	@Test
	void testXgboostSimple() {
		final Arborist arborist = new Arborist();
		final Grove grove = arborist.toGrove("/test-models/xgboostSimple.onnx");

		final Petrify petrify = new Petrify();
		final Fossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);

		assertEquals(1, fossil.predict(new float[] { 1.0f, 2.0f, 3.0f, 4.0f }));
		assertEquals(1, fossil.predict(new float[] { 9.0f, 9.0f, 9.0f, 9.0f }));
		assertEquals(0, fossil.predict(new float[] { 0.0f, 0.0f, 0.0f, 0.0f }));
		assertEquals(0, fossil.predict(new float[] { -1.0f, -2.0f, -1.0f, -2.0f }));
		assertEquals(0, fossil.predict(new float[] { 3.0f, 0.0f, 0.0f, 0.1f }));
		assertEquals(1, fossil.predict(new float[] { 3.0f, 0.0f, 0.0f, 0.3f }));
	}
}
