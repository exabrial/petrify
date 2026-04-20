package com.github.exabrial.petrify.compiler.model;

import java.io.Serializable;

import com.github.exabrial.petrify.compiler.internal.model.GroveSummary;
import com.github.exabrial.petrify.model.PetrifyConstants;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public abstract class Grove implements Serializable {
	private static final long serialVersionUID = 1L;

	public PrecisionMode precisionMode;
	public int[] nodesTreeIds;
	public int[] nodesNodeIds;
	public byte[] nodesModes;
	public int[] nodesFeatureIds;
	public double[] nodesValues;
	public int[] nodesTrueNodeIds;
	public int[] nodesFalseNodeIds;
	public double[] nodesHitRates;
	public int[] nodesMissingValueTracksTrue;
	public byte postTransform;
	public double[] baseValues;
	public ModelMetadata metadata;

	private GroveSummary groveSummary;

	public String summary() {
		if (groveSummary == null) {
			final int totalNodes = nodesTreeIds.length;
			int treeCount = 0;
			int leafCount = 0;
			for (int nodeIdx = 0; nodeIdx < totalNodes; nodeIdx++) {
				if (nodesTreeIds[nodeIdx] >= treeCount) {
					treeCount = nodesTreeIds[nodeIdx] + 1;
				}
				if (nodesModes[nodeIdx] == PetrifyConstants.MODE_LEAF) {
					leafCount++;
				}
			}
			final int branchCount = totalNodes - leafCount;
			groveSummary = new GroveSummary(treeCount, branchCount, leafCount, metadata);
		}
		return groveSummary.toString();
	}
}
