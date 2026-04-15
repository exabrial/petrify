package com.github.exabrial.petrify.compiler.model.exception;

import com.github.exabrial.petrify.model.exception.PetrifyException;

public class UnexpectedCometImpact extends PetrifyException {
	private static final long serialVersionUID = 1L;

	public UnexpectedCometImpact(final Exception e) {
		super(e);
	}
}
