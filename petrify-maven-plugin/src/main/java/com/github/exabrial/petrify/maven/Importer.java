package com.github.exabrial.petrify.maven;

/**
 * Identifies which arborist or vintner is used to parse a model file.
 *
 * <p>Enum constants are lowercase by convention to match the plugin's existing XML configuration
 * syntax; this is unusual for Java but preserves backward compatibility with prior releases where
 * the field was a plain {@code String}.
 */
public enum Importer {
	/**
	 * LightGBM native text-format model (the {@code .txt} produced by {@code saveModelToString}).
	 * Supports both classifier and regressor model types. Handled by
	 * {@code com.github.exabrial.petrify.imprt.lightgbm.LightGbmArborist}.
	 */
	lightgbm,

	/**
	 * ONNX model file (typically {@code .onnx}). Attempts tree-ensemble (arborist) parsing first and
	 * falls back to linear (vintner) parsing if tree parsing fails. Supports both classifier and
	 * regressor model types.
	 */
	onnx,

	/**
	 * scikit-learn linear model serialized to petrify's JSON format (matching the
	 * {@code ScikitLinearModel} schema: {@code type}, {@code coefficients}, {@code intercepts},
	 * {@code classLabels}, {@code postTransform}, and optional metadata fields). Supports both
	 * classifier and regressor model types; tree-based scikit models should be exported to ONNX
	 * instead. Handled by {@code com.github.exabrial.petrify.imprt.scikit.ScikitVintner}.
	 */
	scikit
}
