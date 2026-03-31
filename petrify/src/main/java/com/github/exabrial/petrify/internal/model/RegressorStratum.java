package com.github.exabrial.petrify.internal.model;

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
		leafTargetEntries = grove.toLeafTargetEntries();
	}
}
