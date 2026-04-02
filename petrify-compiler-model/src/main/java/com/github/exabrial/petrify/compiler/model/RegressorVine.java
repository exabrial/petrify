package com.github.exabrial.petrify.compiler.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RegressorVine extends Vine {
	private static final long serialVersionUID = 1L;

	private int nTargets;
}
