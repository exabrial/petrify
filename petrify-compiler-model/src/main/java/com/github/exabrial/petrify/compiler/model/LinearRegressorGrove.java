package com.github.exabrial.petrify.compiler.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class LinearRegressorGrove implements Serializable {
	private static final long serialVersionUID = 1L;

	private float[] coefficients;
	private float[] intercepts;
	private int nTargets;
	private byte postTransform;
	private int nFeatures;
}
