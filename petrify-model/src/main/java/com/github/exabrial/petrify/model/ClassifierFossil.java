/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model;

public interface ClassifierFossil extends Fossil {
	String classify = "classify";
	String predict = "predict";

	// One of these two must be implemented to avoid stack overflow

	default int predict(final double[] features) {
		final float[] narrowed = narrow(features);
		final int prediction = predict(narrowed);
		return prediction;
	}

	default int predict(final float[] features) {
		final double[] widened = widen(features);
		final int prediction = predict(widened);
		return prediction;
	}

	/**
	 * Applies the post_transform to the scores array in place, then returns the index of the class with the highest score (argmax).
	 */
	default int classify(final float[] scores, final byte postTransform, final boolean isBinarySingleScore) {
		switch (postTransform) {
			case PetrifyConstants.POST_TRANSFORM_NONE -> {
			}

			case PetrifyConstants.POST_TRANSFORM_LOGISTIC -> {
				for (int i = 0; i < scores.length; i++) {
					scores[i] = logistic(scores[i]);
				}
			}

			case PetrifyConstants.POST_TRANSFORM_SOFTMAX, PetrifyConstants.POST_TRANSFORM_SOFTMAX_ZERO -> {
				float sumExp = 0.0f;
				for (final float score : scores) {
					sumExp += (float) Math.exp(score);
				}
				for (int i = 0; i < scores.length; i++) {
					scores[i] = softmax(scores[i], sumExp);
				}
			}

			case PetrifyConstants.POST_TRANSFORM_PROBIT -> {
				for (int i = 0; i < scores.length; i++) {
					scores[i] = probit(scores[i]);
				}
			}

			default -> {
				throw new IllegalArgumentException("Unknown post_transform: " + postTransform);
			}
		}

		if (isBinarySingleScore) {
			scores[1] = scores[0];
			scores[0] = 1.0f - scores[1];
		}

		// Argmax: find the index of the highest score
		int bestIdx = 0;
		for (int idx = 1; idx < scores.length; idx++) {
			if (scores[idx] > scores[bestIdx]) {
				bestIdx = idx;
			}
		}
		return bestIdx;
	}

	/**
	 * Applies the post_transform to the scores array in place, then returns the index of the class with the highest score (argmax).
	 */
	default int classify(final double[] scores, final byte postTransform, final boolean isBinarySingleScore) {
		switch (postTransform) {
			case PetrifyConstants.POST_TRANSFORM_NONE -> {
			}

			case PetrifyConstants.POST_TRANSFORM_LOGISTIC -> {
				for (int i = 0; i < scores.length; i++) {
					scores[i] = logistic(scores[i]);
				}
			}

			case PetrifyConstants.POST_TRANSFORM_SOFTMAX, PetrifyConstants.POST_TRANSFORM_SOFTMAX_ZERO -> {
				double sumExp = 0.0;
				for (final double score : scores) {
					sumExp += Math.exp(score);
				}
				for (int i = 0; i < scores.length; i++) {
					scores[i] = softmax(scores[i], sumExp);
				}
			}

			case PetrifyConstants.POST_TRANSFORM_PROBIT -> {
				for (int i = 0; i < scores.length; i++) {
					scores[i] = probit(scores[i]);
				}
			}

			default -> {
				throw new IllegalArgumentException("Unknown post_transform: " + postTransform);
			}
		}

		if (isBinarySingleScore) {
			scores[1] = scores[0];
			scores[0] = 1.0d - scores[1];
		}

		// Argmax: find the index of the highest score
		int bestIdx = 0;
		for (int idx = 1; idx < scores.length; idx++) {
			if (scores[idx] > scores[bestIdx]) {
				bestIdx = idx;
			}
		}
		return bestIdx;
	}
}
