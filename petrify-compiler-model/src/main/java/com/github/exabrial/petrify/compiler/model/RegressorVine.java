package com.github.exabrial.petrify.compiler.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RegressorVine extends Vine {
	private static final long serialVersionUID = 1L;

	public int nTargets;
}
