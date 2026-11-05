package com.github.exabrial.petrify.compiler.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A {@link Grove} for classification. The {@code class*} arrays map each leaf to the class weights it adds to the per-class scores;
 * {@code classLabelsInt64s} holds the label returned for each class index.
 */
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
