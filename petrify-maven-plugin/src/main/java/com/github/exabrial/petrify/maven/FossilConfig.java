package com.github.exabrial.petrify.maven;

import lombok.Data;

/**
 * Per-model configuration for the {@code fossilize} goal. Appears as {@code <fossil>} entries under
 * the {@code <fossils>} list in a POM {@code <configuration>} block.
 *
 * <p>Example:
 *
 * <pre>
 * &lt;fossil&gt;
 *   &lt;modelFile&gt;my-model.txt&lt;/modelFile&gt;
 *   &lt;importer&gt;lightgbm&lt;/importer&gt;
 *   &lt;modelType&gt;regressor&lt;/modelType&gt;
 *   &lt;targetPackageName&gt;com.example.models&lt;/targetPackageName&gt;
 * &lt;/fossil&gt;
 * </pre>
 */
@Data
public class FossilConfig {

	/**
	 * Directory containing the model file. If omitted, defaults to {@code src/main/models} relative
	 * to the project base directory.
	 */
	private String modelDirectory;

	/**
	 * Filename of the model to compile, resolved relative to {@link #modelDirectory}. Required.
	 */
	private String modelFile;

	/**
	 * Which importer parses the model file. See {@link Importer} for valid values.
	 *
	 * <p>Required. Eclipse and IntelliJ will offer autocomplete once this enum type is published.
	 */
	private Importer importer;

	/**
	 * Whether this model is a classifier or a regressor. See {@link ModelType} for valid values.
	 *
	 * <p>Required. Eclipse and IntelliJ will offer autocomplete once this enum type is published.
	 */
	private ModelType modelType;

	/**
	 * Package name of the generated fossil class. Required.
	 */
	private String targetPackageName;

	/**
	 * Base name of the generated fossil class; the suffix {@code Fossil} is appended automatically.
	 * If omitted, the class name is derived from {@link #modelFile} by stripping the extension and
	 * camel-casing the remainder.
	 */
	private String targetClassName;

	/**
	 * Optional human-readable model name baked into the generated fossil's metadata. Retrievable at
	 * runtime via {@code Fossil.getModelName()}.
	 */
	private String modelName;

	/**
	 * Optional model version string baked into the generated fossil's metadata. Retrievable at
	 * runtime via {@code Fossil.getModelVersion()}.
	 */
	private String modelVersion;

	/**
	 * Comma-separated override for the feature-name list baked into the generated fossil. Useful when
	 * the source model does not embed feature names or embeds incorrect ones. Retrievable at runtime
	 * via {@code Fossil.getFeatureNames()}.
	 */
	private String featureNames;

	/**
	 * If true, discards any feature-name list parsed from the source model. Has no effect when
	 * {@link #featureNames} is set (the override takes precedence regardless).
	 */
	private boolean ignoreFeatureNamesFromModel;

	public String[] resolveFeatureNames() {
		final String[] result;
		if (featureNames != null && !featureNames.isEmpty()) {
			result = featureNames.split(",");
		} else {
			result = null;
		}
		return result;
	}

	public String resolveClassName() {
		final String result;
		if (targetClassName != null && !targetClassName.isEmpty()) {
			result = targetClassName + "Fossil";
		} else {
			final int dotIdx = modelFile.lastIndexOf('.');
			final String stem;
			if (dotIdx > 0) {
				stem = modelFile.substring(0, dotIdx);
			} else {
				stem = modelFile;
			}
			result = sanitizeIdentifier(stem) + "Fossil";
		}
		return result;
	}

	protected String sanitizeIdentifier(final String raw) {
		final StringBuilder sb = new StringBuilder(raw.length());
		boolean capitalizeNext = true;
		for (int charIdx = 0; charIdx < raw.length(); charIdx++) {
			final char ch = raw.charAt(charIdx);
			if (Character.isJavaIdentifierPart(ch) && (ch != '_')) {
				if (sb.isEmpty() && !Character.isJavaIdentifierStart(ch)) {
					capitalizeNext = true;
				} else if (capitalizeNext) {
					sb.append(Character.toUpperCase(ch));
					capitalizeNext = false;
				} else {
					sb.append(ch);
					capitalizeNext = false;
				}
			} else {
				capitalizeNext = true;
			}
		}
		return sb.toString();
	}
}
