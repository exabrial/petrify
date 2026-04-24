package com.github.exabrial.petrify;

import static com.github.exabrial.petrify.testing.GroveGenerator.THRESHOLD;
import static com.github.exabrial.petrify.testing.GroveGenerator.WEIGHT_FALSE;
import static com.github.exabrial.petrify.testing.GroveGenerator.WEIGHT_TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.compiler.model.RegressorGrove;
import com.github.exabrial.petrify.model.PetrifyConstants;
import com.github.exabrial.petrify.model.RegressionFossil;
import com.github.exabrial.petrify.testing.GroveGenerator;

class PetrifyBaseValuesTest {
	private static final double BASE_VALUE = 5.0;

	private final GroveGenerator groveGenerator = new GroveGenerator(PrecisionMode.F64);
	private final Petrify petrify = new Petrify();

	@Test
	void test_nonZeroBaseValue() throws Exception {
		final RegressorGrove grove = groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_EQ);
		grove.baseValues = new double[] { BASE_VALUE }; // predict = baseValue + leafWeight

		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);
		assertEquals(BASE_VALUE + WEIGHT_TRUE, fossil.predict(new double[] { THRESHOLD }));
		assertEquals(BASE_VALUE + WEIGHT_FALSE, fossil.predict(new double[] { THRESHOLD + 1.0 }));
	}
}
