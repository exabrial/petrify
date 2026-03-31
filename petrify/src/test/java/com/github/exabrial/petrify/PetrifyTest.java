package com.github.exabrial.petrify;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
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
		grove.setNodesTreeIds(new int[] { 0, 0, 0 });
		grove.setNodesNodeIds(new int[] { 0, 1, 2 });
		grove.setNodesModes(new byte[] { 1, 0, 0 });
		grove.setNodesFeatureIds(new int[] { 0, 0, 0 });
		grove.setNodesValues(new float[] { 4.0f, 0.0f, 0.0f });
		grove.setNodesTrueNodeIds(new int[] { 1, 0, 0 });
		grove.setNodesFalseNodeIds(new int[] { 2, 0, 0 });
		grove.setNodesHitRates(new float[] { 1.0f, 1.0f, 1.0f });
		grove.setNodesMissingValueTracksTrue(new int[] { 0, 0, 0 });

		grove.setClassTreeIds(new int[] { 0, 0 });
		grove.setClassNodeIds(new int[] { 1, 2 });
		grove.setClassIds(new int[] { 0, 1 });
		grove.setClassWeights(new float[] { 1.0f, 1.0f });

		grove.setClassLabelsInt64s(new long[] { 0L, 1L });
		grove.setPostTransform((byte) 0);
		return grove;
	}
}
