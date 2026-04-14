package com.github.exabrial.petrify.testing.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.petrify.model.ClassifierFossil;

@TestInstance(Lifecycle.PER_CLASS)
class DecisionTreeSimpleTest {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private ClassifierFossil fossil;

	@BeforeAll
	void beforeAll() throws Exception {
		fossil = new DecisionTreeSimpleFossil();
	}

	@BeforeEach
	void beforeEach(final TestInfo testInfo) {
		log.info("beforeEach() starting test:{}", testInfo.getDisplayName());
	}

	@Test
	void testPredict_0() {
		final int actual = fossil.predict(new float[] { 4.0f });
		assertEquals(0, actual);
	}

	@Test
	void testPredict_1() {
		final int actual = fossil.predict(new float[] { 4.1f });
		assertEquals(1, actual);
	}
}
