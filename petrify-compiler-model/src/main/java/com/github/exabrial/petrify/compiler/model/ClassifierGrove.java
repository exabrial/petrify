package com.github.exabrial.petrify.compiler.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ClassifierGrove extends Grove {
	private static final long serialVersionUID = 1L;

	public int[] classTreeIds;
	public int[] classNodeIds;
	public int[] classIds;
	public double[] classWeights;
	public long[] classLabelsInt64s;
}
