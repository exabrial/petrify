/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClassifierFossilTest {
	private final ClassifierFossil fossil = new ClassifierFossil() {
		private static final long serialVersionUID = 1L;
	};

	@Test
	void testClassifyFloatArrayByteBoolean() {
		assertEquals(1, fossil.classify(new float[] { 0.1f, 0.9f }, PetrifyConstants.POST_TRANSFORM_NONE, false));
		assertEquals(0, fossil.classify(new float[] { 0.9f, 0.1f }, PetrifyConstants.POST_TRANSFORM_NONE, false));
		assertEquals(1, fossil.classify(new float[] { 0.9f, 0.1f }, PetrifyConstants.POST_TRANSFORM_NONE, true));
		assertEquals(1, fossil.classify(new float[] { 0.1f, 0.9f }, PetrifyConstants.POST_TRANSFORM_LOGISTIC, false));
	}

	@Test
	void testClassifyDoubleArrayByteBoolean() {
		assertEquals(1, fossil.classify(new double[] { 0.1, 0.9 }, PetrifyConstants.POST_TRANSFORM_NONE, false));
		assertEquals(0, fossil.classify(new double[] { 0.9, 0.1 }, PetrifyConstants.POST_TRANSFORM_NONE, false));
		assertEquals(1, fossil.classify(new double[] { 0.9, 0.1 }, PetrifyConstants.POST_TRANSFORM_NONE, true));
		assertEquals(1, fossil.classify(new double[] { 0.1, 0.9 }, PetrifyConstants.POST_TRANSFORM_LOGISTIC, false));
	}
}
