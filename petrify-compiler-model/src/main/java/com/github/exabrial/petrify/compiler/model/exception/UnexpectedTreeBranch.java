package com.github.exabrial.petrify.compiler.model.exception;

import com.github.exabrial.petrify.model.exception.PetrifyException;

public class UnexpectedTreeBranch extends PetrifyException {
	private static final long serialVersionUID = 1L;

	public UnexpectedTreeBranch(final String message) {
		super(message);
	}
}
