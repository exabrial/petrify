package com.github.exabrial.petrify.internal.model;

import java.util.List;
import java.util.Map;

import com.github.exabrial.petrify.compiler.model.Grove;
import com.github.exabrial.petrify.compiler.model.LeafClassEntry;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Stratum {
	public final Grove grove;
	public final Map<Long, Integer> nodeIndex;
	public final Map<Long, List<LeafClassEntry>> leafClassEntries;
	public final boolean isBinarySingleScore;

	public Stratum(final Grove grove) {
		this.grove = grove;
		nodeIndex = grove.toNodeIndex();
		leafClassEntries = grove.toLeafClassEntries();
		isBinarySingleScore = grove.toIsBinarySingleScore();
	}
}
