/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.petrify.model.Fossil;
import com.github.exabrial.petrify.model.exception.FeatureUnconformity;
import com.github.exabrial.petrify.model.exception.FossilUnconformity;

import lombok.Getter;
import lombok.Setter;

public class FeatureMapper {
	protected final Logger log;
	protected final String mappedFor;
	protected final List<String> featureNames;
	@Getter
	@Setter
	protected boolean tossExceptionOnMissingFeatures = false;

	public FeatureMapper(final Fossil fossil) {
		log = LoggerFactory.getLogger(FeatureMapper.class);
		final List<String> names = fossil.getFeatureNames();
		if (names.isEmpty()) {
			throw new FossilUnconformity("Fossil does not contain feature name metadata");
		} else {
			mappedFor = fossil.getClass().getName();
			featureNames = names;
		}
	}

	public FeatureMapper(final List<String> featureNames, final Fossil fossil) {
		log = LoggerFactory.getLogger(FeatureMapper.class);
		if (featureNames.isEmpty()) {
			throw new FossilUnconformity("Feature names list cannot be empty");
		} else {
			mappedFor = fossil.getClass().getName();
			this.featureNames = new ArrayList<>(featureNames);
		}
	}

	public float[] mapToF32(final Map<String, Object> features) throws FeatureUnconformity {
		// ok so... the Java streams api does not have a mapToFloat? Alrighty then.
		final List<String> missingFeatures = new ArrayList<>();
		final float[] result = new float[featureNames.size()];
		for (int idx = 0; idx < featureNames.size(); idx++) {
			final String featureName = featureNames.get(idx);
			if (!features.containsKey(featureName)) {
				missingFeatures.add(featureName);
			}
			result[idx] = floatOf(featureName, features);
		}
		if (!missingFeatures.isEmpty()) {
			log.warn("mapToF32() detected missing features! mappedFor:{} missingFeatureCount:{} missingFeatures:{}", mappedFor,
					missingFeatures.size(), missingFeatures);
			if (tossExceptionOnMissingFeatures) {
				throw new FeatureUnconformity("mapToF32()", mappedFor, missingFeatures);
			}
		}
		return result;
	}

	public double[] mapToF64(final Map<String, Object> features) throws FeatureUnconformity {
		final List<String> missingFeatures = new ArrayList<>();
		for (final String featureName : featureNames) {
			if (!features.containsKey(featureName)) {
				missingFeatures.add(featureName);
			}
		}
		if (!missingFeatures.isEmpty()) {
			log.warn("mapToF64() detected missing features! mappedFor:{} missingFeatureCount:{} missingFeatures:{}", mappedFor,
					missingFeatures.size(), missingFeatures);
			if (tossExceptionOnMissingFeatures) {
				throw new FeatureUnconformity("mapToF64()", mappedFor, missingFeatures);
			}
		}
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
		result.append(", tossExceptionOnMissingFeatures=").append(tossExceptionOnMissingFeatures);
		result.append(']');
		return result.toString();
	}
}
