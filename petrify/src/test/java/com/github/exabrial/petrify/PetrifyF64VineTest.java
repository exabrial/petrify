package com.github.exabrial.petrify;

import static com.github.exabrial.petrify.testing.VineGenerator.CLASS_A;
import static com.github.exabrial.petrify.testing.VineGenerator.CLASS_B;
import static com.github.exabrial.petrify.testing.VineGenerator.COEFFICIENT;
import static com.github.exabrial.petrify.testing.VineGenerator.INTERCEPT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.model.ClassifierFossil;
import com.github.exabrial.petrify.model.RegressionFossil;
import com.github.exabrial.petrify.testing.VineGenerator;

class PetrifyF64VineTest {
	private final VineGenerator vineGenerator = new VineGenerator(PrecisionMode.F64);
	private final Petrify petrify = new Petrify();

	@Test
	void test_regressorVine() throws Exception {
		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(), vineGenerator.singleTermRegressorVine());
		assertEquals(INTERCEPT, fossil.predict(new double[] { 0.0 }));
		assertEquals(INTERCEPT + COEFFICIENT, fossil.predict(new double[] { 1.0 }));
		assertEquals(INTERCEPT + COEFFICIENT * -1.0, fossil.predict(new double[] { -1.0 }));
	}

	@Test
	void test_classifierVine() throws Exception {
		final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(), vineGenerator.singleTermClassifierVine());
		assertEquals(CLASS_A, fossil.predict(new double[] { 0.0 }));
		assertEquals(CLASS_A, fossil.predict(new double[] { 10.0 }));
		assertEquals(CLASS_B, fossil.predict(new double[] { -10.0 }));
	}
}
