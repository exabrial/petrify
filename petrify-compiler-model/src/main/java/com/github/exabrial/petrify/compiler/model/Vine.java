package com.github.exabrial.petrify.compiler.model;

import java.io.Serializable;

import lombok.Data;

@Data
public abstract class Vine implements Serializable {
	private static final long serialVersionUID = 1L;

	protected float[] coefficients;
	protected float[] intercepts;
	protected byte postTransform;
	protected int nFeatures;
}
