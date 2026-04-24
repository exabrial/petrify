/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class PetrifyConstantsTest {
	@Test
	void testPackLong() {
		assertEquals(0L, PetrifyConstants.packLong(0, 0));
		assertEquals(PetrifyConstants.packLong(1, 2), PetrifyConstants.packLong(1, 2));
		assertNotEquals(PetrifyConstants.packLong(1, 2), PetrifyConstants.packLong(2, 1));
		assertNotEquals(PetrifyConstants.packLong(0, 1), PetrifyConstants.packLong(1, 0));
		assertEquals(1L, PetrifyConstants.packLong(0, 1));
		assertEquals(1L << 32, PetrifyConstants.packLong(1, 0));
		assertEquals(1L << 32 | 1L, PetrifyConstants.packLong(1, 1));
		assertEquals(5L << 32 | Integer.toUnsignedLong(-1), PetrifyConstants.packLong(5, -1));
	}
}
