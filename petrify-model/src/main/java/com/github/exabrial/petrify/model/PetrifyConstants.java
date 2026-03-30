/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model;

public interface PetrifyConstants {
	// ONNX operator types
	String OP_TREE_ENSEMBLE_CLASSIFIER = "TreeEnsembleClassifier";
	String OP_TREE_ENSEMBLE = "TreeEnsemble";

	// Node modes
	byte MODE_LEAF = 0;
	byte MODE_BRANCH_LEQ = 1;
	byte MODE_BRANCH_LT = 2;

	// Post-transform types
	byte POST_TRANSFORM_NONE = 0;
	byte POST_TRANSFORM_SOFTMAX = 1;
	byte POST_TRANSFORM_LOGISTIC = 2;
	byte POST_TRANSFORM_SOFTMAX_ZERO = 3;
	byte POST_TRANSFORM_PROBIT = 4;

	/**
	 * Pack to integers into a single long. Useful for making composite indexes.
	 *
	 * @param mostSignificant
	 *          mostSignificant
	 * @param leastSignificant
	 *          leastSignificant
	 * @return ints packed as a single long
	 */
	static long packLong(final int mostSignificant, final int leastSignificant) {
		return (long) mostSignificant << 32 | Integer.toUnsignedLong(leastSignificant);
	}
}
