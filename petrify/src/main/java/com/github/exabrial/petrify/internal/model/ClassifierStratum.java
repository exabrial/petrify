package com.github.exabrial.petrify.internal.model;

import static com.github.exabrial.petrify.model.PetrifyConstants.packLong;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
import com.github.exabrial.petrify.compiler.model.LeafClassEntry;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ClassifierStratum extends Stratum {
	public final ClassifierGrove classifierGrove;
	public final Map<Long, List<LeafClassEntry>> leafClassEntries;
	public final boolean isBinarySingleScore;

	public ClassifierStratum(final ClassifierGrove grove) {
		super(grove);
		classifierGrove = grove;
		leafClassEntries = toLeafClassEntries(grove);
		isBinarySingleScore = toIsBinarySingleScore(grove);
	}

	protected Map<Long, List<LeafClassEntry>> toLeafClassEntries(final ClassifierGrove grove) {
		final Map<Long, List<LeafClassEntry>> map = new HashMap<>();
		for (int index = 0; index < grove.classNodeIds.length; index++) {
			final long key = packLong(grove.classTreeIds[index], grove.classNodeIds[index]);
			map.computeIfAbsent(key, (@SuppressWarnings("unused") final Object unused) -> new ArrayList<>())
					.add(new LeafClassEntry(grove.classIds[index], grove.classWeights[index]));
		}
		return map;
	}

	protected boolean toIsBinarySingleScore(final ClassifierGrove grove) {
		if (grove.classLabelsInt64s.length != 2) {
			return false;
		} else {
			for (final int classId : grove.classIds) {
				if (classId != 0) {
					return false;
				}
			}
			return true;
		}
	}
}
