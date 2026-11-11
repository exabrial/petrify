package com.github.exabrial.petrify.compiler.model;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Optional descriptive data for a {@link Grove} or {@link Vine}. Each non-null field is compiled into a getter on the {@code Fossil};
 * {@code featureNames} (in column order) enables {@code FeatureMapper}.
 */
@ToString
@EqualsAndHashCode
public class ModelMetadata implements Serializable {
	private static final long serialVersionUID = 1L;

	public String modelFamily;
	public String modelName;
	public Integer majorVersion;
	public Integer minorVersion;
	public String[] featureNames;
}
