package com.github.exabrial.petrify.imprt.scikit;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import jakarta.json.bind.annotation.JsonbProperty;

import lombok.Data;

/**
 * The scikit-learn JSON model file, as read by JSON-B. Field names follow the JSON keys in the readme's JSON Format section.
 * {@code ScikitVintner} validates it and converts it to a {@code Vine}.
 */
@Data
class ScikitLinearModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private String type;
	@JsonbProperty("post_transform")
	private String postTransform;
	@JsonbProperty("class_labels")
	private long[] classLabels;
	private double[] intercepts;
	private double[][] coefficients;
	@JsonbProperty("feature_names")
	private String[] featureNames;
	@JsonbProperty("model_family")
	private String modelFamily;
	@JsonbProperty("model_name")
	private String modelName;
	@JsonbProperty("major_version")
	private Integer majorVersion;
	@JsonbProperty("minor_version")
	private Integer minorVersion;
	@JsonbProperty("pre_transformers")
	private Map<Integer, List<ScikitPreTransformer>> preTransformers;
}
