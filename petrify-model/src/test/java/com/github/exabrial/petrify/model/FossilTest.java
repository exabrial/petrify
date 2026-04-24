/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FossilTest {
	private final Fossil fossil = new Fossil() {
		private static final long serialVersionUID = 1L;
	};

	@Test
	void testLogisticFloat() {
		assertEquals(0.5f, fossil.logistic(0.0f));
		assertEquals(1.0f, fossil.logistic(100.0f), 1e-6f);
		assertEquals(0.0f, fossil.logistic(-100.0f), 1e-6f);
	}

	@Test
	void testLogisticDouble() {
		assertEquals(0.5d, fossil.logistic(0.0d));
		assertEquals(1.0d, fossil.logistic(100.0d), 1e-12d);
		assertEquals(0.0d, fossil.logistic(-100.0d), 1e-12d);
	}

	@Test
	void testSoftmaxFloatFloat() {
		final float sumExp = (float) (Math.exp(1.0) + Math.exp(2.0) + Math.exp(3.0));
		assertEquals((float) Math.exp(1.0f) / sumExp, fossil.softmax(1.0f, sumExp));
		assertEquals((float) Math.exp(3.0f) / sumExp, fossil.softmax(3.0f, sumExp));
	}

	@Test
	void testSoftmaxDoubleDouble() {
		final double sumExp = Math.exp(1.0) + Math.exp(2.0) + Math.exp(3.0);
		assertEquals(Math.exp(1.0) / sumExp, fossil.softmax(1.0, sumExp));
		assertEquals(Math.exp(3.0) / sumExp, fossil.softmax(3.0, sumExp));
	}

	@Test
	void testProbitFloat() {
		assertEquals(0.5f, fossil.probit(0.0f));
		assertEquals(1.0f, fossil.probit(100.0f), 1e-6f);
		assertEquals(0.0f, fossil.probit(-100.0f), 1e-6f);
	}

	@Test
	void testProbitDouble() {
		assertEquals(0.5d, fossil.probit(0.0d));
		assertEquals(1.0d, fossil.probit(100.0d), 1e-12d);
		assertEquals(0.0d, fossil.probit(-100.0d), 1e-12d);
	}

	@Test
	void testWiden() {
		final float[] narrow = { 1.5f, -2.5f, 0.0f };
		final double[] widened = fossil.widen(narrow);
		assertEquals(3, widened.length);
		assertEquals(1.5d, widened[0]);
		assertEquals(-2.5d, widened[1]);
		assertEquals(0.0d, widened[2]);
	}

	@Test
	void testNarrow() {
		final double[] wide = { 1.5d, -2.5d, 0.0d };
		final float[] narrowed = fossil.narrow(wide);
		assertEquals(3, narrowed.length);
		assertEquals(1.5f, narrowed[0]);
		assertEquals(-2.5f, narrowed[1]);
		assertEquals(0.0f, narrowed[2]);
	}
}
