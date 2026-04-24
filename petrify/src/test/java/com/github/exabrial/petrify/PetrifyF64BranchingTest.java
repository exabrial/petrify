package com.github.exabrial.petrify;

import static com.github.exabrial.petrify.testing.GroveGenerator.CLASS_FALSE;
import static com.github.exabrial.petrify.testing.GroveGenerator.CLASS_TRUE;
import static com.github.exabrial.petrify.testing.GroveGenerator.THRESHOLD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedCometImpact;
import com.github.exabrial.petrify.model.ClassifierFossil;
import com.github.exabrial.petrify.model.PetrifyConstants;
import com.github.exabrial.petrify.testing.GroveGenerator;

class PetrifyF64BranchingTest {
	private static final double BELOW = Math.nextDown(THRESHOLD);
	private static final double ABOVE = Math.nextUp(THRESHOLD);

	private final GroveGenerator groveGenerator = new GroveGenerator(PrecisionMode.F64);
	private final Petrify petrify = new Petrify();

	// Compile a bunch of 3 node trees. Modify just the branching mode.

	@Test
	void test_MODE_BRANCH_EQ() throws Exception {
		final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitGrove(PetrifyConstants.MODE_BRANCH_EQ));
		assertEquals(CLASS_FALSE, fossil.predict(new double[] { BELOW }));
		assertEquals(CLASS_TRUE, fossil.predict(new double[] { THRESHOLD }));
		assertEquals(CLASS_FALSE, fossil.predict(new double[] { ABOVE }));
	}

	@Test
	void test_MODE_BRANCH_NEQ() throws Exception {
		final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitGrove(PetrifyConstants.MODE_BRANCH_NEQ, 1));
		assertEquals(CLASS_TRUE, fossil.predict(new double[] { BELOW }));
		assertEquals(CLASS_FALSE, fossil.predict(new double[] { THRESHOLD }));
		assertEquals(CLASS_TRUE, fossil.predict(new double[] { ABOVE }));
	}

	@Test
	void test_MODE_BRANCH_LEQ() throws Exception {
		final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitGrove(PetrifyConstants.MODE_BRANCH_LEQ));
		assertEquals(CLASS_TRUE, fossil.predict(new double[] { BELOW }));
		assertEquals(CLASS_TRUE, fossil.predict(new double[] { THRESHOLD }));
		assertEquals(CLASS_FALSE, fossil.predict(new double[] { ABOVE }));
	}

	@Test
	void test_MODE_BRANCH_LT() throws Exception {
		final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitGrove(PetrifyConstants.MODE_BRANCH_LT));
		assertEquals(CLASS_TRUE, fossil.predict(new double[] { BELOW }));
		assertEquals(CLASS_FALSE, fossil.predict(new double[] { THRESHOLD }));
		assertEquals(CLASS_FALSE, fossil.predict(new double[] { ABOVE }));
	}

	@Test
	void test_MODE_BRANCH_GEQ() throws Exception {
		final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitGrove(PetrifyConstants.MODE_BRANCH_GEQ));
		assertEquals(CLASS_FALSE, fossil.predict(new double[] { BELOW }));
		assertEquals(CLASS_TRUE, fossil.predict(new double[] { THRESHOLD }));
		assertEquals(CLASS_TRUE, fossil.predict(new double[] { ABOVE }));
	}

	@Test
	void test_MODE_BRANCH_GT() throws Exception {
		final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitGrove(PetrifyConstants.MODE_BRANCH_GT));
		assertEquals(CLASS_FALSE, fossil.predict(new double[] { BELOW }));
		assertEquals(CLASS_FALSE, fossil.predict(new double[] { THRESHOLD }));
		assertEquals(CLASS_TRUE, fossil.predict(new double[] { ABOVE }));
	}

	@Test
	void test_MODE_BRANCH_EQ_missingTracksTrue_rejected() {
		assertThrows(UnexpectedCometImpact.class,
				() -> petrify.fossilize(MethodHandles.lookup(), groveGenerator.singleSplitGrove(PetrifyConstants.MODE_BRANCH_EQ, 1)));
	}

	@Test
	void test_MODE_BRANCH_NEQ_missingTracksFalse_rejected() {
		assertThrows(UnexpectedCometImpact.class,
				() -> petrify.fossilize(MethodHandles.lookup(), groveGenerator.singleSplitGrove(PetrifyConstants.MODE_BRANCH_NEQ, 0)));
	}

	@Test
	void test_unknownBranchMode_rejected() {
		assertThrows(UnexpectedCometImpact.class,
				() -> petrify.fossilize(MethodHandles.lookup(), groveGenerator.singleSplitGrove((byte) 99)));
	}
}
