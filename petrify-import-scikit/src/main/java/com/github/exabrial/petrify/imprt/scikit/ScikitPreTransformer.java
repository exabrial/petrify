package com.github.exabrial.petrify.imprt.scikit;

import java.io.Serializable;

import com.github.exabrial.petrify.compiler.model.PreTransformerType;

import lombok.Data;

/**
 * One entry of {@code pre_transformers} in the scikit JSON: {@code {"transformer": "max", "arg": 20000}}. {@code arg} is {@code null}
 * when the JSON omits it.
 */
@Data
class ScikitPreTransformer implements Serializable {
	private static final long serialVersionUID = 1L;

	private PreTransformerType transformer;
	private Double arg;
}
