/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model.util;

import java.util.List;
import java.util.Map;

import com.github.exabrial.petrify.model.Fossil;
import com.github.exabrial.petrify.model.exception.FossilUnconformity;

public class FeatureMapper {
	private final String mappedFor;
	private final List<String> featureNames;

	public FeatureMapper(final Fossil fossil) {
		final List<String> names = fossil.getFeatureNames();
		if (names.isEmpty()) {
			throw new FossilUnconformity("Fossil does not contain feature name metadata");
		} else {
			mappedFor = fossil.getClass().getName();
			featureNames = names;
		}
	}

	public float[] mapToF32(final Map<String, Object> features) {
		// ok so... the Java streams api does not have a mapToFloat? Alrighty then.
		final float[] result = new float[featureNames.size()];
		for (int idx = 0; idx < featureNames.size(); idx++) {
			result[idx] = floatOf(featureNames.get(idx), features);
		}
		return result;
	}

	public double[] mapToF64(final Map<String, Object> features) {
		return featureNames.stream().mapToDouble((final String featureName) -> doubleOf(featureName, features)).toArray();
	}

	protected static float floatOf(final String featureName, final Map<String, Object> features) {
		final Object feature = features.get(featureName);
		final float result;
		if (feature == null) {
			result = Float.NaN;
		} else if (feature instanceof final Number number) {
			result = number.floatValue();
		} else if (feature instanceof final Boolean bool) {
			result = bool ? 1.0f : 0.0f;
		} else {
			throw new FossilUnconformity(featureName, feature.getClass().getName());
		}
		return result;
	}

	protected static double doubleOf(final String featureName, final Map<String, Object> features) {
		final Object feature = features.get(featureName);
		final double result;
		if (feature == null) {
			result = Double.NaN;
		} else if (feature instanceof final Number number) {
			result = number.doubleValue();
		} else if (feature instanceof final Boolean bool) {
			result = bool ? 1.0d : 0.0d;
		} else {
			throw new FossilUnconformity(featureName, feature.getClass().getName());
		}
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("FeatureMapper@").append(Integer.toHexString(System.identityHashCode(this)));
		result.append("[mappedFor=").append(mappedFor);
		result.append(", featureCount=").append(featureNames.size());
		result.append(']');
		return result.toString();
	}
}
