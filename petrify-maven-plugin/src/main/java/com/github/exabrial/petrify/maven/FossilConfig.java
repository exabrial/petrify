package com.github.exabrial.petrify.maven;

import lombok.Data;

@Data
public class FossilConfig {
	private String modelDirectory;
	private String modelFile;
	private String importer;
	private String modelType;
	private String packageName;
	private String className;

	public String resolveClassName() {
		final String result;
		if (className != null && !className.isEmpty()) {
			result = className + "Fossil";
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
