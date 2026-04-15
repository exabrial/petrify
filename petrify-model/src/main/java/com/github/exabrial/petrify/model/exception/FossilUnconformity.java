/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model.exception;

public class FossilUnconformity extends PetrifyException {
	private static final long serialVersionUID = 1L;

	public FossilUnconformity(final String message) {
		super(message);
	}

	public FossilUnconformity(final String featureName, final String typeName) {
		super("Unsupported feature type for featureName:" + featureName + " typeName:" + typeName);
	}
}
