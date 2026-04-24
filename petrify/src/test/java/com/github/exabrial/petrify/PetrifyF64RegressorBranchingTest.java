package com.github.exabrial.petrify;

import static com.github.exabrial.petrify.testing.GroveGenerator.THRESHOLD;
import static com.github.exabrial.petrify.testing.GroveGenerator.WEIGHT_FALSE;
import static com.github.exabrial.petrify.testing.GroveGenerator.WEIGHT_TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedCometImpact;
import com.github.exabrial.petrify.model.PetrifyConstants;
import com.github.exabrial.petrify.model.RegressionFossil;
import com.github.exabrial.petrify.testing.GroveGenerator;

class PetrifyF64RegressorBranchingTest {
	private static final double BELOW = Math.nextDown(THRESHOLD);
	private static final double ABOVE = Math.nextUp(THRESHOLD);

	private final GroveGenerator groveGenerator = new GroveGenerator(PrecisionMode.F64);
	private final Petrify petrify = new Petrify();

	@Test
	void test_MODE_BRANCH_EQ() throws Exception {
		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_EQ));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { BELOW }));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { THRESHOLD }));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { ABOVE }));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { Double.NaN }));
	}

	@Test
	void test_MODE_BRANCH_NEQ() throws Exception {
		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_NEQ, 1));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { BELOW }));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { THRESHOLD }));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { ABOVE }));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { Double.NaN }));
	}

	@Test
	void test_MODE_BRANCH_LEQ() throws Exception {
		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_LEQ));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { BELOW }));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { THRESHOLD }));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { ABOVE }));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { Double.NaN }));
	}

	@Test
	void test_MODE_BRANCH_LT() throws Exception {
		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_LT));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { BELOW }));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { THRESHOLD }));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { ABOVE }));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { Double.NaN }));
	}

	@Test
	void test_MODE_BRANCH_GEQ() throws Exception {
		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_GEQ));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { BELOW }));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { THRESHOLD }));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { ABOVE }));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { Double.NaN }));
	}

	@Test
	void test_MODE_BRANCH_GT() throws Exception {
		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_GT));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { BELOW }));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { THRESHOLD }));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { ABOVE }));
		assertEquals(WEIGHT_FALSE, fossil.predict(new double[] { Double.NaN }));
	}

	@Test
	void test_MODE_BRANCH_LEQ_NaN_missingTracksTrue() throws Exception {
		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_LEQ, 1));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { Double.NaN }));
	}

	@Test
	void test_MODE_BRANCH_LT_NaN_missingTracksTrue() throws Exception {
		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_LT, 1));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { Double.NaN }));
	}

	@Test
	void test_MODE_BRANCH_GEQ_NaN_missingTracksTrue() throws Exception {
		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_GEQ, 1));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { Double.NaN }));
	}

	@Test
	void test_MODE_BRANCH_GT_NaN_missingTracksTrue() throws Exception {
		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(),
				groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_GT, 1));
		assertEquals(WEIGHT_TRUE, fossil.predict(new double[] { Double.NaN }));
	}

	@Test
	void test_MODE_BRANCH_EQ_missingTracksTrue_rejected() {
		assertThrows(UnexpectedCometImpact.class,
				() -> petrify.fossilize(MethodHandles.lookup(), groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_EQ, 1)));
	}

	@Test
	void test_MODE_BRANCH_NEQ_missingTracksFalse_rejected() {
		assertThrows(UnexpectedCometImpact.class,
				() -> petrify.fossilize(MethodHandles.lookup(), groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_NEQ, 0)));
	}

	@Test
	void test_unknownBranchMode_rejected() {
		assertThrows(UnexpectedCometImpact.class,
				() -> petrify.fossilize(MethodHandles.lookup(), groveGenerator.singleSplitRegressorGrove((byte) 99)));
	}
}
