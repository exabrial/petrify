package com.github.exabrial.petrify.compiler.model;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Intermediate representation of a linear model (from ONNX or scikit-learn JSON): a score per output is the intercept plus the dot
 * product of the features with that output's coefficients, followed by {@code postTransform}. Compiled by {@code Petrify} into a
 * {@code Fossil}.
 */
@ToString
@EqualsAndHashCode
public abstract class Vine implements Serializable {
	private static final long serialVersionUID = 1L;

	public PrecisionMode precisionMode;
	public double[] coefficients;
	public double[] intercepts;
	public byte postTransform;
	public int nFeatures;
	public ModelMetadata metadata;
}
