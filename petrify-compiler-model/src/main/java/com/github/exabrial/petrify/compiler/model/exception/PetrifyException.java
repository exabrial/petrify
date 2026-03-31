package com.github.exabrial.petrify.compiler.model.exception;

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
