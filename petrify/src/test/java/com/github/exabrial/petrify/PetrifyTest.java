package com.github.exabrial.petrify;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.model.ClassifierFossil;

class PetrifyTest {

	@Test
	void test() {
		final Petrify petrify = new Petrify();
		final ClassifierGrove grove = exampleGrove();
		final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);
		final int prediction = fossil.predict(new float[] { 6.0f });

		assertEquals(1, prediction);
	}

	static ClassifierGrove exampleGrove() {
		final ClassifierGrove grove = new ClassifierGrove();
		grove.nodesTreeIds = new int[] { 0, 0, 0 };
		grove.nodesNodeIds = new int[] { 0, 1, 2 };
		grove.nodesModes = new byte[] { 1, 0, 0 };
		grove.nodesFeatureIds = new int[] { 0, 0, 0 };
		grove.nodesValues = new double[] { 4.0, 0.0, 0.0 };
		grove.nodesTrueNodeIds = new int[] { 1, 0, 0 };
		grove.nodesFalseNodeIds = new int[] { 2, 0, 0 };
		grove.nodesHitRates = new double[] { 1.0, 1.0, 1.0 };
		grove.nodesMissingValueTracksTrue = new int[] { 0, 0, 0 };

		grove.classTreeIds = new int[] { 0, 0 };
		grove.classNodeIds = new int[] { 1, 2 };
		grove.classIds = new int[] { 0, 1 };
		grove.classWeights = new double[] { 1.0, 1.0 };

		grove.classLabelsInt64s = new long[] { 0L, 1L };
		grove.postTransform = (byte) 0;
		grove.precisionMode = PrecisionMode.F64;
		return grove;
	}
}
