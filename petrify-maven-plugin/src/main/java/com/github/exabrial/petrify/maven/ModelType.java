package com.github.exabrial.petrify.maven;

/**
 * Identifies whether a model is a classifier or regressor, which determines how the model's output
 * is interpreted and which {@code Fossil} interface the generated class implements.
 *
 * <p>Enum constants are lowercase by convention to match the plugin's existing XML configuration
 * syntax; this is unusual for Java but preserves backward compatibility with prior releases where
 * the field was a plain {@code String}.
 */
public enum ModelType {
	/**
	 * A classification model. The generated fossil implements
	 * {@code com.github.exabrial.petrify.model.ClassifierFossil} and its {@code predict} method
	 * returns the predicted class label.
	 */
	classifier,

	/**
	 * A regression model. The generated fossil implements
	 * {@code com.github.exabrial.petrify.model.RegressionFossil} and its {@code predict} method
	 * returns a continuous numeric score.
	 */
	regressor
}
