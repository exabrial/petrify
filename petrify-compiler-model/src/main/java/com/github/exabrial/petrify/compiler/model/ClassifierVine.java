package com.github.exabrial.petrify.compiler.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClassifierVine extends Vine {
	private static final long serialVersionUID = 1L;

	private long[] classlabelsInts;
	private int multiClass;
	private int nClasses;

	public boolean toIsBinarySingleScore() {
		if (classlabelsInts.length != 2) {
			return false;
		} else {
			return multiClass == 0;
		}
	}
}
