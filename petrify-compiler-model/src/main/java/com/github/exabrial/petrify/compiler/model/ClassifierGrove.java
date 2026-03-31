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
public class ClassifierGrove extends Grove {
	private static final long serialVersionUID = 1L;

	protected int[] classTreeIds;
	protected int[] classNodeIds;
	protected int[] classIds;
	protected float[] classWeights;

	protected long[] classLabelsInt64s;

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
