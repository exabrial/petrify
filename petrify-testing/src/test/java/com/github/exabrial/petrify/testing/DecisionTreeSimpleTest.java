package com.github.exabrial.petrify.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.Petrify;
import com.github.exabrial.petrify.imprt.Arborist;
import com.github.exabrial.petrify.model.Fossil;
import com.github.exabrial.petrify.model.Grove;

class DecisionTreeSimpleTest {

	@Test
	void testDecisionTreeSimple() {
		final Arborist arborist = new Arborist();
		final Grove grove = arborist.toGrove("/test-models/decisionTreeSimple.onnx");

		final Petrify petrify = new Petrify();
		final Fossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);

		assertEquals(0, fossil.predict(new float[] { 1.0f, 2.0f, 3.0f, 4.0f }));
		assertEquals(1, fossil.predict(new float[] { 9.0f, 9.0f, 9.0f, 9.0f }));
	}
}
