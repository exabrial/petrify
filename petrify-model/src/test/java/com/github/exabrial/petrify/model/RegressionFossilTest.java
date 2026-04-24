/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RegressionFossilTest {
	private final RegressionFossil fossil = new RegressionFossil() {
		private static final long serialVersionUID = 1L;
	};

	@Test
	void testAggregateFloatByte() {
		assertEquals(3.5f, fossil.aggregate(3.5f, PetrifyConstants.POST_TRANSFORM_NONE));
		assertEquals(0.5f, fossil.aggregate(0.0f, PetrifyConstants.POST_TRANSFORM_LOGISTIC));
		assertEquals(0.5f, fossil.aggregate(0.0f, PetrifyConstants.POST_TRANSFORM_PROBIT));
	}

	@Test
	void testAggregateDoubleByte() {
		assertEquals(3.5d, fossil.aggregate(3.5d, PetrifyConstants.POST_TRANSFORM_NONE));
		assertEquals(0.5d, fossil.aggregate(0.0d, PetrifyConstants.POST_TRANSFORM_LOGISTIC));
		assertEquals(0.5d, fossil.aggregate(0.0d, PetrifyConstants.POST_TRANSFORM_PROBIT));
	}
}
