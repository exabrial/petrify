package com.github.exabrial.petrify.compiler.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A {@link Grove} for regression. The {@code target*} arrays map each leaf to the weight it adds to the score of each of the
 * {@code nTargets} outputs.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RegressorGrove extends Grove {
	private static final long serialVersionUID = 1L;

	public int[] targetTreeIds;
	public int[] targetNodeIds;
	public int[] targetIds;
	public double[] targetWeights;
	public int nTargets;
	public byte aggregateFunction;
}
