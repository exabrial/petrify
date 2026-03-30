package com.github.exabrial.petrify.internal.model.exception;

public class UnexpectedCometImpact extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public UnexpectedCometImpact(final Exception e) {
		super(e);
	}

	public UnexpectedCometImpact(final String string) {
		super(string);
	}
}
