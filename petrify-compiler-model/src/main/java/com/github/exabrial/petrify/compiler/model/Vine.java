package com.github.exabrial.petrify.compiler.model;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public abstract class Vine implements Serializable {
	private static final long serialVersionUID = 1L;

	public float[] coefficients;
	public float[] intercepts;
	public byte postTransform;
	public int nFeatures;
}
