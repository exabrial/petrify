package com.github.exabrial.petrify.compiler.model;

import static com.github.exabrial.petrify.model.PetrifyConstants.packLong;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RegressorGrove extends Grove {
	private static final long serialVersionUID = 1L;

	protected int[] targetTreeIds;
	protected int[] targetNodeIds;
	protected int[] targetIds;
	protected float[] targetWeights;

	protected int nTargets;
	protected byte aggregateFunction;

	public Map<Long, List<LeafTargetEntry>> toLeafTargetEntries() {
		final Map<Long, List<LeafTargetEntry>> map = new HashMap<>();
		for (int index = 0; index < targetNodeIds.length; index++) {
			final long key = packLong(targetTreeIds[index], targetNodeIds[index]);
			map.computeIfAbsent(key, (@SuppressWarnings("unused") final Object unused) -> new ArrayList<>())
					.add(new LeafTargetEntry(targetIds[index], targetWeights[index]));
		}
		return map;
	}
}
