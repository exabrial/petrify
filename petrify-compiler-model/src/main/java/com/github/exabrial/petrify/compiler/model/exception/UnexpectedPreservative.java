package com.github.exabrial.petrify.compiler.model.exception;

/**
 * Thrown when an ONNX graph contains operators or attributes that Petrify cannot process at this time. Man, it'd be cool if it did
 * though. How about creating a PR?
 */
public class UnexpectedPreservative extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public UnexpectedPreservative(final String message) {
		super(message);
	}
}
