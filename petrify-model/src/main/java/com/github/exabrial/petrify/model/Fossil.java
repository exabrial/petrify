/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public interface Fossil extends Serializable {

	default List<String> getFeatureNames() {
		return Collections.emptyList();
	}

	default String getModelName() {
		return null;
	}

	default String getModelVersion() {
		return null;
	}

	default float logistic(final float score) {
		return 1.0f / (1.0f + (float) Math.exp(-score));
	}

	default double logistic(final double score) {
		return 1.0d / (1.0d + Math.exp(-score));
	}

	default float softmax(final float score, final float sumExp) {
		return (float) Math.exp(score) / sumExp;
	}

	default double softmax(final double score, final double sumExp) {
		return Math.exp(score) / sumExp;
	}

	default float probit(final float score) {
		return 0.5f * (1.0f + (float) Math.tanh(score * (float) PetrifyConstants.INVERSE_SQRT_2));
	}

	default double probit(final double score) {
		return 0.5d * (1.0d + Math.tanh(score * PetrifyConstants.INVERSE_SQRT_2));
	}

	default double[] widen(final float[] narrow) {
		final double[] widened = new double[narrow.length];
		for (int i = 0; i < narrow.length; i++) {
			widened[i] = narrow[i];
		}
		return widened;
	}

	default float[] narrow(final double[] wide) {
		final float[] narrowed = new float[wide.length];
		for (int i = 0; i < wide.length; i++) {
			narrowed[i] = (float) wide[i];
		}
		return narrowed;
	}

	static String fossilToString(final Fossil fossil) {
		final StringBuilder result = new StringBuilder();
		result.append(fossil.getClass().getName());
		result.append('@').append(Integer.toHexString(System.identityHashCode(fossil)));
		result.append('[');
		boolean first = true;
		final String modelName = fossil.getModelName();
		if (modelName != null) {
			result.append("modelName=").append(modelName);
			first = false;
		}
		final String modelVersion = fossil.getModelVersion();
		if (modelVersion != null) {
			if (!first) {
				result.append(", ");
			}
			result.append("modelVersion=").append(modelVersion);
			first = false;
		}
		final List<String> featureNames = fossil.getFeatureNames();
		if (!featureNames.isEmpty()) {
			if (!first) {
				result.append(", ");
			}
			result.append("featureCount=").append(featureNames.size());
		}
		result.append(']');
		return result.toString();
	}
}
