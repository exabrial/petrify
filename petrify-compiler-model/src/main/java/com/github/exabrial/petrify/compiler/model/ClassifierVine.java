package com.github.exabrial.petrify.compiler.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

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
