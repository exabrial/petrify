package com.github.exabrial.petrify.compiler.model.exception;

public class UnexpectedCometImpact extends PetrifyException {
	private static final long serialVersionUID = 1L;

	public UnexpectedCometImpact(final Exception e) {
		super(e);
	}

	public UnexpectedCometImpact(final String string) {
		super(string);
	}
}
