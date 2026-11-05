package com.github.exabrial.petrify.compiler.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A {@link Vine} for regression: one coefficient row and intercept per target. The compiled {@code Fossil} returns the score of the
 * first target.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RegressorVine extends Vine {
	private static final long serialVersionUID = 1L;

	public int nTargets;
}
