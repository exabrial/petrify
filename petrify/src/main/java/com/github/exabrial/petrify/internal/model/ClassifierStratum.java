package com.github.exabrial.petrify.internal.model;

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
		leafClassEntries = grove.toLeafClassEntries();
		isBinarySingleScore = grove.toIsBinarySingleScore();
	}
}
