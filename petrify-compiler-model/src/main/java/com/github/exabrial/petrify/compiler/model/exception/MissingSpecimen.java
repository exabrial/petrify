package com.github.exabrial.petrify.compiler.model.exception;

import com.github.exabrial.petrify.model.exception.PetrifyException;

/**
 * Thrown when an ONNX model cannot be located at the specified classpath location.
 */
public class MissingSpecimen extends PetrifyException {
	private static final long serialVersionUID = 1L;

	public MissingSpecimen(final String message) {
		super(message);
	}
}
