package com.github.exabrial.petrify.testing;

import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.model.PetrifyConstants;

public class GroveGenerator {
	public static final int CLASS_TRUE = 1;
	public static final int CLASS_FALSE = 0;
	public static final double THRESHOLD = 100.0;

	public final PrecisionMode precision;

	public GroveGenerator(final PrecisionMode precision) {
		this.precision = precision;
	}

	public ClassifierGrove singleSplitGrove(final byte mode) {
		return singleSplitGrove(mode, 0);
	}

	public ClassifierGrove singleSplitGrove(final byte mode, final int missingValueTracksTrue) {
		final ClassifierGrove grove = new ClassifierGrove();

		grove.precisionMode = precision;
		grove.postTransform = PetrifyConstants.POST_TRANSFORM_NONE;
		grove.baseValues = new double[] { 0.0, 0.0 };

		grove.nodesTreeIds = new int[] { 0, 0, 0 }; // all three nodes belong to tree 0
		grove.nodesNodeIds = new int[] { 0, 1, 2 }; // node ids within tree 0: root=0, true-leaf=1, false-leaf=2
		grove.nodesModes = new byte[] { mode, PetrifyConstants.MODE_LEAF, PetrifyConstants.MODE_LEAF }; // root is branch; children are
																																																		// leaves
		grove.nodesFeatureIds = new int[] { 0, 0, 0 }; // root tests features[0]; unused at leaves
		grove.nodesValues = new double[] { THRESHOLD, 0.0, 0.0 }; // root threshold; unused at leaves
		grove.nodesTrueNodeIds = new int[] { 1, 0, 0 }; // root true-branch -> node 1; unused at leaves
		grove.nodesFalseNodeIds = new int[] { 2, 0, 0 }; // root false-branch -> node 2; unused at leaves
		grove.nodesHitRates = new double[] { 1.0, 1.0, 1.0 }; // unused at inference
		grove.nodesMissingValueTracksTrue = new int[] { missingValueTracksTrue, 0, 0 }; // root missing policy; unused at leaves

		grove.classTreeIds = new int[] { 0, 0 }; // both leaf contributions from tree 0
		grove.classNodeIds = new int[] { 1, 2 }; // node 1 = true-leaf, node 2 = false-leaf
		grove.classIds = new int[] { 0, 0 }; // both target class 0 (binary single-score)
		grove.classWeights = new double[] { 1.0, -1.0 }; // true-leaf +1.0, false-leaf -1.0
		grove.classLabelsInt64s = new long[] { CLASS_FALSE, CLASS_TRUE }; // two class labels -> binary classifier

		return grove;
	}
}
