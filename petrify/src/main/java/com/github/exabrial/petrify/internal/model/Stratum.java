package com.github.exabrial.petrify.internal.model;

import static com.github.exabrial.petrify.model.PetrifyConstants.packLong;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.github.exabrial.petrify.compiler.model.Grove;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Stratum {
	public final Grove grove;
	public final Map<Long, Integer> nodeIndex;
	public final int[] treeRootIds;

	public Stratum(final Grove grove) {
		this.grove = grove;
		nodeIndex = toNodeIndex(grove);
		treeRootIds = Arrays.stream(grove.nodesTreeIds).distinct().toArray();
	}

	protected Map<Long, Integer> toNodeIndex(final Grove grove) {
		final Map<Long, Integer> nodeIndex = new HashMap<>();
		final int nodeCount = grove.nodesNodeIds.length;
		for (int i = 0; i < nodeCount; i++) {
			final long key = packLong(grove.nodesTreeIds[i], grove.nodesNodeIds[i]);
			nodeIndex.put(key, i);
		}
		return nodeIndex;
	}
}
