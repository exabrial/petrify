package com.github.exabrial.petrify.compiler.model;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public abstract class Grove implements Serializable {
	private static final long serialVersionUID = 1L;

	public int[] nodesTreeIds;
	public int[] nodesNodeIds;
	public byte[] nodesModes;
	public int[] nodesFeatureIds;
	public float[] nodesValues;
	public int[] nodesTrueNodeIds;
	public int[] nodesFalseNodeIds;
	public float[] nodesHitRates;
	public int[] nodesMissingValueTracksTrue;
	public byte postTransform;
	public float[] baseValues;
}
