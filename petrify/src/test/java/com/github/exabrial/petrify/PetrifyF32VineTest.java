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

class PetrifyF32VineTest {
	private final VineGenerator vineGenerator = new VineGenerator(PrecisionMode.F32);
	private final Petrify petrify = new Petrify();

	@Test
	void test_regressorVine() throws Exception {
		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(), vineGenerator.singleTermRegressorVine());
		assertEquals((float) INTERCEPT, fossil.predict(new float[] { 0.0f }));
		assertEquals((float) (INTERCEPT + COEFFICIENT), fossil.predict(new float[] { 1.0f }));
		assertEquals((float) (INTERCEPT + COEFFICIENT * -1.0), fossil.predict(new float[] { -1.0f }));
	}

	@Test
	void test_classifierVine() throws Exception {
		final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(), vineGenerator.singleTermClassifierVine());
		assertEquals(CLASS_A, fossil.predict(new float[] { 0.0f }));
		assertEquals(CLASS_A, fossil.predict(new float[] { 10.0f }));
		assertEquals(CLASS_B, fossil.predict(new float[] { -10.0f }));
	}
}
