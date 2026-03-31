/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model;

import java.io.Serializable;

public interface Fossil extends Serializable {

	default float logistic(final float score) {
		return 1.0f / (1.0f + (float) Math.exp(-score));
	}

	default float softmax(final float score, final float sumExp) {
		return (float) Math.exp(score) / sumExp;
	}

	default float probit(final float score) {
		return 0.5f * (1.0f + (float) Math.tanh(score * 0.7071067811865476f));
	}
}
