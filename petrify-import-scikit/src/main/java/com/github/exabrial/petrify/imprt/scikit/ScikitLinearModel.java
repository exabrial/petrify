package com.github.exabrial.petrify.imprt.scikit;

import java.io.Serializable;

import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Data;

@Data
public class ScikitLinearModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private String type;
	@JsonbProperty("post_transform")
	private String postTransform;
	@JsonbProperty("class_labels")
	private long[] classLabels;
	private double[] intercepts;
	private double[][] coefficients;
}
