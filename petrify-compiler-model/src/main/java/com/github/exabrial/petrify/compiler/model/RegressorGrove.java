package com.github.exabrial.petrify.compiler.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RegressorGrove extends Grove {
	private static final long serialVersionUID = 1L;

	public int[] targetTreeIds;
	public int[] targetNodeIds;
	public int[] targetIds;
	public float[] targetWeights;
	public int nTargets;
	public byte aggregateFunction;
}
