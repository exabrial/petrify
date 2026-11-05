/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.github.exabrial.petrify.model;

/**
 * A {@link Fossil} for regression. The generated class implements {@code predict} for its precision; the other overload converts the
 * features and delegates. {@code aggregate} applies the post-transform to the summed score.
 */
public interface RegressionFossil extends Fossil {
	String aggregate = "aggregate";
	String predict = "predict";

	// One of these two must be implemented to avoid stack overflow

	default double predict(final double[] features) {
		final float[] narrowed = narrow(features);
		final double prediction = predict(narrowed);
		return prediction;
	}

	default float predict(final float[] features) {
		final double[] widened = widen(features);
		final float prediction = (float) predict(widened);
		return prediction;
	}

	/**
	 * Applies the post_transform to the accumulated score and returns the result.
	 */
	default float aggregate(final float score, final byte postTransform) {
		final float result;
		switch (postTransform) {
			case PetrifyConstants.POST_TRANSFORM_NONE -> {
				result = score;
			}

			case PetrifyConstants.POST_TRANSFORM_LOGISTIC -> {
				result = logistic(score);
			}

			case PetrifyConstants.POST_TRANSFORM_PROBIT -> {
				result = probit(score);
			}

			default -> {
				throw new IllegalArgumentException("Unknown post_transform: " + postTransform);
			}
		}
		return result;
	}

	/**
	 * Applies the post_transform to the accumulated score and returns the result.
	 */
	default double aggregate(final double score, final byte postTransform) {
		final double result;
		switch (postTransform) {
			case PetrifyConstants.POST_TRANSFORM_NONE -> {
				result = score;
			}

			case PetrifyConstants.POST_TRANSFORM_LOGISTIC -> {
				result = logistic(score);
			}

			case PetrifyConstants.POST_TRANSFORM_PROBIT -> {
				result = probit(score);
			}

			default -> {
				throw new IllegalArgumentException("Unknown post_transform: " + postTransform);
			}
		}
		return result;
	}
}
