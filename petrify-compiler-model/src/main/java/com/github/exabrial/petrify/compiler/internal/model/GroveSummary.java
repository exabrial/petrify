package com.github.exabrial.petrify.compiler.internal.model;

import com.github.exabrial.petrify.compiler.model.ModelMetadata;

/**
 * Counts of trees, branches, and leaves in a {@code Grove}, cached by {@code Grove.summary()} for log output.
 */
public record GroveSummary(int treeCount, int branchCount, int leafCount, ModelMetadata metadata) {

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("GroveSummary[treeCount=").append(treeCount);
		result.append(", branchCount=").append(branchCount);
		result.append(", leafCount=").append(leafCount);
		if (metadata != null) {
			if (metadata.modelFamily != null) {
				result.append(", modelFamily=").append(metadata.modelFamily);
			}
			if (metadata.modelName != null) {
				result.append(", modelName=").append(metadata.modelName);
			}
			if (metadata.majorVersion != null) {
				result.append(", majorVersion=").append(metadata.majorVersion);
			}
			if (metadata.minorVersion != null) {
				result.append(", minorVersion=").append(metadata.minorVersion);
			}
			if (metadata.featureNames != null) {
				result.append(", featureCount=").append(metadata.featureNames.length);
			}
		}
		result.append("]");
		return result.toString();
	}
}
