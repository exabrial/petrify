package com.github.exabrial.petrify.internal.model;

import java.util.Map;

import com.github.exabrial.petrify.compiler.model.Grove;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Stratum {
	public final Grove grove;
	public final Map<Long, Integer> nodeIndex;

	public Stratum(final Grove grove) {
		this.grove = grove;
		nodeIndex = grove.toNodeIndex();
	}
}
