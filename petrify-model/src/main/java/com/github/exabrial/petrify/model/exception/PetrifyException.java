/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model.exception;

public abstract class PetrifyException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PetrifyException(final String message) {
		super(message);
	}

	public PetrifyException(final Throwable cause) {
		super(cause);
	}

	public PetrifyException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public PetrifyException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
