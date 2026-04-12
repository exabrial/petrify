package com.github.exabrial.petrify.compiler.model;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public abstract class Vine implements Serializable {
	private static final long serialVersionUID = 1L;

	public PrecisionMode precisionMode = PrecisionMode.F32;
	public double[] coefficients;
	public double[] intercepts;
	public byte postTransform;
	public int nFeatures;
}
