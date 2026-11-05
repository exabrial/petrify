package com.github.exabrial.petrify.compiler.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A {@link Vine} for classification: one coefficient row and intercept per class, or a single row for a binary one-vs-rest model
 * ({@code multiClass == 0}). The winning class index maps to its label in {@code classlabelsInts}.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ClassifierVine extends Vine {
	private static final long serialVersionUID = 1L;

	public long[] classlabelsInts;
	public int multiClass;
	public int nClasses;

	public boolean isBinarySingleScore() {
		if (classlabelsInts.length != 2) {
			return false;
		} else {
			return multiClass == 0;
		}
	}
}
