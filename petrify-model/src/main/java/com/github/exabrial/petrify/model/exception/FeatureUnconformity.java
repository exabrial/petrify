/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model.exception;

import java.util.List;

public class FeatureUnconformity extends PetrifyException {
	private static final long serialVersionUID = 1L;

	public FeatureUnconformity(final String methodName, final String mappedFor, final List<String> missingFeatures) {
		super(methodName + " missing features for " + mappedFor + ": " + missingFeatures);
	}
}
