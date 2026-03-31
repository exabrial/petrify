package com.github.exabrial.petrify.compiler.model;

import static com.github.exabrial.petrify.model.PetrifyConstants.packLong;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public abstract class Grove implements Serializable {
	private static final long serialVersionUID = 1L;

	protected int[] nodesTreeIds;
	protected int[] nodesNodeIds;
	protected byte[] nodesModes;
	protected int[] nodesFeatureIds;
	protected float[] nodesValues;
	protected int[] nodesTrueNodeIds;
	protected int[] nodesFalseNodeIds;
	protected float[] nodesHitRates;
	protected int[] nodesMissingValueTracksTrue;

	protected byte postTransform;
	protected float[] baseValues;

	public int[] getTreeRootIds() {
		return Arrays.stream(nodesTreeIds).distinct().toArray();
	}

	public Map<Long, Integer> toNodeIndex() {
		final Map<Long, Integer> nodeIndex = new HashMap<>();
		final int nodeCount = nodesNodeIds.length;
		for (int i = 0; i < nodeCount; i++) {
			final long key = packLong(nodesTreeIds[i], nodesNodeIds[i]);
			nodeIndex.put(key, i);
		}
		return nodeIndex;
	}
}
