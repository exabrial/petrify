package com.github.exabrial.petrify.compiler.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class LinearClassifierGrove implements Serializable {
	private static final long serialVersionUID = 1L;

	private float[] coefficients;
	private float[] intercepts;
	private long[] classlabelsInts;
	private int multiClass;
	private byte postTransform;
	private int nFeatures;
	private int nClasses;

	public boolean toIsBinarySingleScore() {
		if (classlabelsInts.length != 2) {
			return false;
		} else {
			return multiClass == 0;
		}
	}
}
