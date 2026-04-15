package com.github.exabrial.petrify.compiler.model;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ModelMetadata implements Serializable {
	private static final long serialVersionUID = 1L;

	public String modelName;
	public String modelVersion;
	public String[] featureNames;
}
