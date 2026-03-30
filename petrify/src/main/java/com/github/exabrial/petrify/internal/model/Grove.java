package com.github.exabrial.petrify.internal.model;

import static com.github.exabrial.petrify.model.PetrifyConstants.packLong;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class Grove implements Serializable {
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

	protected int[] classTreeIds;
	protected int[] classNodeIds;
	protected int[] classIds;
	protected float[] classWeights;

	protected long[] classLabelsInt64s;
	protected byte postTransform;

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

	public Map<Long, List<LeafClassEntry>> toLeafClassEntries() {
		final Map<Long, List<LeafClassEntry>> map = new HashMap<>();
		for (int index = 0; index < classNodeIds.length; index++) {
			final long key = packLong(classTreeIds[index], classNodeIds[index]);
			map.computeIfAbsent(key, (@SuppressWarnings("unused") final Object unused) -> new ArrayList<>())
					.add(new LeafClassEntry(classIds[index], classWeights[index]));
		}
		return map;
	}

	public boolean toIsBinarySingleScore() {
		if (classLabelsInt64s.length != 2) {
			return false;
		} else {
			for (final int classId : classIds) {
				if (classId != 0) {
					return false;
				}
			}
			return true;
		}
	}
}