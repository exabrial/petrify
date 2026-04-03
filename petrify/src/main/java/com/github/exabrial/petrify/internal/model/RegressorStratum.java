package com.github.exabrial.petrify.internal.model;

import static com.github.exabrial.petrify.model.PetrifyConstants.packLong;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.exabrial.petrify.compiler.model.LeafTargetEntry;
import com.github.exabrial.petrify.compiler.model.RegressorGrove;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RegressorStratum extends Stratum {
	public final RegressorGrove regressorGrove;
	public final Map<Long, List<LeafTargetEntry>> leafTargetEntries;

	public RegressorStratum(final RegressorGrove grove) {
		super(grove);
		regressorGrove = grove;
		leafTargetEntries = toLeafTargetEntries(grove);
	}

	protected Map<Long, List<LeafTargetEntry>> toLeafTargetEntries(final RegressorGrove grove) {
		final Map<Long, List<LeafTargetEntry>> map = new HashMap<>();
		for (int index = 0; index < grove.targetNodeIds.length; index++) {
			final long key = packLong(grove.targetTreeIds[index], grove.targetNodeIds[index]);
			map.computeIfAbsent(key, (@SuppressWarnings("unused") final Object unused) -> new ArrayList<>())
					.add(new LeafTargetEntry(grove.targetIds[index], grove.targetWeights[index]));
		}
		return map;
	}
}
