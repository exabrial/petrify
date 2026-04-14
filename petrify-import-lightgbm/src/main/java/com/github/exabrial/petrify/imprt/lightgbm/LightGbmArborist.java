package com.github.exabrial.petrify.imprt.lightgbm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
import com.github.exabrial.petrify.compiler.model.Grove;
import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.compiler.model.RegressorGrove;
import com.github.exabrial.petrify.compiler.model.exception.MissingSpecimen;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedCometImpact;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedTreeBranch;
import com.github.exabrial.petrify.imprt.Arborist;
import com.github.exabrial.petrify.model.PetrifyConstants;

public class LightGbmArborist implements Arborist {
	@Override
	public <T extends Grove> T toGrove(final String classpathLocation) {
		try (final InputStream is = getClass().getResourceAsStream(classpathLocation)) {
			if (is == null) {
				throw new MissingSpecimen("LightGbm model not found on classpath: " + classpathLocation);
			} else {
				final byte[] bytes = is.readAllBytes();
				return toGrove(bytes);
			}
		} catch (final IOException ioException) {
			throw new UnexpectedCometImpact(ioException);
		}
	}

	@Override
	public <T extends Grove> T toGrove(final byte[] bytes) {
		final String modelString = new String(bytes, StandardCharsets.UTF_8);
		return parse(modelString);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Grove> T parse(final String modelString) {
		final ParseMachine machine = new ParseMachine();
		try (final BufferedReader reader = new BufferedReader(new StringReader(modelString))) {
			String line;
			while ((line = reader.readLine()) != null) {
				machine.feedLine(line);
			}
		} catch (final IOException ioException) {
			throw new UnexpectedCometImpact(ioException);
		}
		return (T) machine.buildGrove();
	}

	protected class ParseMachine {
		protected static final String MARKER_MAGIC = "tree";
		protected static final String MARKER_TREE_PREFIX = "Tree=";
		protected static final String MARKER_END_OF_TREES = "end of trees";
		protected static final String MARKER_FEATURE_IMPORTANCES = "feature_importances:";
		protected static final String MARKER_PARAMETERS = "parameters:";
		protected static final String MARKER_END_OF_PARAMETERS = "end of parameters";

		protected static final String KEY_VERSION = "version";
		protected static final String KEY_NUM_TREE_PER_ITERATION = "num_tree_per_iteration";
		protected static final String KEY_OBJECTIVE = "objective";

		protected static final String KEY_NUM_LEAVES = "num_leaves";
		protected static final String KEY_SPLIT_FEATURE = "split_feature";
		protected static final String KEY_THRESHOLD = "threshold";
		protected static final String KEY_DECISION_TYPE = "decision_type";
		protected static final String KEY_LEFT_CHILD = "left_child";
		protected static final String KEY_RIGHT_CHILD = "right_child";
		protected static final String KEY_LEAF_VALUE = "leaf_value";

		protected static final String VALUE_V4 = "v4";

		protected static final String OBJECTIVE_REGRESSION = "regression";
		protected static final String OBJECTIVE_REGRESSION_L2 = "regression_l2";
		protected static final String OBJECTIVE_MSE = "mean_squared_error";
		protected static final String OBJECTIVE_MSE_SHORT = "mse";
		protected static final String OBJECTIVE_RMSE = "l2_root_mean_squared_error";
		protected static final String OBJECTIVE_RMSE_SHORT = "rmse";
		protected static final String OBJECTIVE_REGRESSION_L1 = "regression_l1";
		protected static final String OBJECTIVE_MAE = "mean_absolute_error";
		protected static final String OBJECTIVE_MAE_SHORT = "mae";
		protected static final String OBJECTIVE_HUBER = "huber";
		protected static final String OBJECTIVE_FAIR = "fair";
		protected static final String OBJECTIVE_POISSON = "poisson";
		protected static final String OBJECTIVE_QUANTILE = "quantile";
		protected static final String OBJECTIVE_MAPE = "mape";
		protected static final String OBJECTIVE_GAMMA = "gamma";
		protected static final String OBJECTIVE_TWEEDIE = "tweedie";
		protected static final String OBJECTIVE_BINARY = "binary";
		protected static final String OBJECTIVE_CROSS_ENTROPY = "cross_entropy";
		protected static final String OBJECTIVE_CROSS_ENTROPY_LAMBDA = "cross_entropy_lambda";
		protected static final String OBJECTIVE_MULTICLASS = "multiclass";
		protected static final String OBJECTIVE_SOFTMAX = "softmax";
		protected static final String OBJECTIVE_MULTICLASSOVA = "multiclassova";
		protected static final String OBJECTIVE_MULTICLASS_OVA = "multiclass_ova";
		protected static final String OBJECTIVE_OVA = "ova";
		protected static final String OBJECTIVE_OVR = "ovr";

		private ParseState state = ParseState.MAGIC;
		private String[] split;
		private boolean regressor;
		private byte postTransform;
		private int numTreePerIteration;
		private final ArrayList<Tree> trees = new ArrayList<>();
		private Tree currentTree;

		enum ParseState {
			MAGIC, HEADER, TREE, FEATURE_IMPORTANCES, PARAMETERS, DONE
		}

		protected void feedLine(final String line) {
			final String trimmedLine = line.trim();
			if (trimmedLine.isEmpty()) {
				return;
			} else {
				final ParseState nextState = detectTransition(trimmedLine);
				if (nextState != null) {
					state = nextState;
				} else {
					dispatch(trimmedLine);
				}
			}
		}

		protected ParseState detectTransition(final String line) {
			final ParseState result;
			if (line.startsWith(MARKER_TREE_PREFIX)) {
				final int treeIndex = Integer.parseInt(line.substring(MARKER_TREE_PREFIX.length()));
				while (trees.size() <= treeIndex) {
					trees.add(null);
				}
				currentTree = new Tree();
				trees.set(treeIndex, currentTree);
				result = ParseState.TREE;
			} else {
				result = switch (line) {
					case MARKER_END_OF_TREES -> ParseState.FEATURE_IMPORTANCES;
					case MARKER_FEATURE_IMPORTANCES -> ParseState.FEATURE_IMPORTANCES;
					case MARKER_PARAMETERS -> ParseState.PARAMETERS;
					case MARKER_END_OF_PARAMETERS -> ParseState.DONE;
					case null -> null;
					default -> null;
				};
			}
			return result;
		}

		protected void dispatch(final String line) {
			switch (state) {
				case MAGIC -> magic(line);
				case HEADER -> headerLine(line);
				case TREE -> treeLine(line);
				case FEATURE_IMPORTANCES -> featureImportanceLine(line);
				case PARAMETERS -> parameterLine(line);
				case DONE -> {
				}
			}
		}

		protected boolean isDone() {
			return state == ParseState.DONE;
		}

		protected void headerLine(final String line) {
			split = split(line);
			switch (split[0]) {
				case KEY_VERSION -> headerVersion(split[1]);
				case KEY_NUM_TREE_PER_ITERATION -> headerNumTreePerIteration(split[1]);
				case KEY_OBJECTIVE -> headerObjective(split[1]);
				default -> {
				}
			}
		}

		protected void treeLine(final String line) {
			split = split(line);
			switch (split[0]) {
				case KEY_NUM_LEAVES -> currentTree.numLeaves = Integer.parseInt(split[1]);
				case KEY_SPLIT_FEATURE -> currentTree.splitFeature = parseIntArray(split[1]);
				case KEY_THRESHOLD -> currentTree.threshold = parseDoubleArray(split[1]);
				case KEY_DECISION_TYPE -> currentTree.decisionType = parseIntArray(split[1]);
				case KEY_LEFT_CHILD -> currentTree.leftChild = parseIntArray(split[1]);
				case KEY_RIGHT_CHILD -> currentTree.rightChild = parseIntArray(split[1]);
				case KEY_LEAF_VALUE -> currentTree.leafValue = parseDoubleArray(split[1]);
				default -> {
				}
			}
		}

		protected void featureImportanceLine(final String line) {
			// Noop
		}

		protected void parameterLine(final String line) {
			// Noop
		}

		@SuppressWarnings("unchecked")
		protected <T extends Grove> T buildGrove() {
			int totalNodes = 0;
			int totalLeaves = 0;
			for (final Tree tree : trees) {
				final int numInternalNodes = tree.numLeaves - 1;
				totalNodes += numInternalNodes + tree.numLeaves;
				totalLeaves += tree.numLeaves;
			}

			final int[] nodesTreeIds = new int[totalNodes];
			final int[] nodesNodeIds = new int[totalNodes];
			final byte[] nodesModes = new byte[totalNodes];
			final int[] nodesFeatureIds = new int[totalNodes];
			final double[] nodesValues = new double[totalNodes];
			final int[] nodesTrueNodeIds = new int[totalNodes];
			final int[] nodesFalseNodeIds = new int[totalNodes];
			final int[] nodesMissingValueTracksTrue = new int[totalNodes];
			final double[] nodesHitRates = new double[totalNodes];
			Arrays.fill(nodesHitRates, 1.0d);

			final int[] leafTreeIds = new int[totalLeaves];
			final int[] leafNodeIds = new int[totalLeaves];
			final int[] leafIds = new int[totalLeaves];
			final double[] leafWeights = new double[totalLeaves];

			int nodeOffset = 0;
			int leafOffset = 0;

			for (int treeIdx = 0; treeIdx < trees.size(); treeIdx++) {
				final Tree tree = trees.get(treeIdx);
				final int numInternalNodes = tree.numLeaves - 1;
				final int leafBaseNodeId = numInternalNodes;

				for (int internalNodeIdx = 0; internalNodeIdx < numInternalNodes; internalNodeIdx++) {
					final int flatIdx = nodeOffset + internalNodeIdx;
					nodesTreeIds[flatIdx] = treeIdx;
					nodesNodeIds[flatIdx] = internalNodeIdx;
					nodesModes[flatIdx] = PetrifyConstants.MODE_BRANCH_LEQ;
					nodesFeatureIds[flatIdx] = tree.splitFeature[internalNodeIdx];
					nodesValues[flatIdx] = tree.threshold[internalNodeIdx];
					nodesTrueNodeIds[flatIdx] = remapChild(tree.leftChild[internalNodeIdx], leafBaseNodeId);
					nodesFalseNodeIds[flatIdx] = remapChild(tree.rightChild[internalNodeIdx], leafBaseNodeId);
					nodesMissingValueTracksTrue[flatIdx] = (tree.decisionType[internalNodeIdx] & 0x2) != 0 ? 1 : 0;
				}

				for (int leafIdx = 0; leafIdx < tree.numLeaves; leafIdx++) {
					final int flatIdx = nodeOffset + leafBaseNodeId + leafIdx;
					nodesTreeIds[flatIdx] = treeIdx;
					nodesNodeIds[flatIdx] = leafBaseNodeId + leafIdx;
					nodesModes[flatIdx] = PetrifyConstants.MODE_LEAF;
					nodesFeatureIds[flatIdx] = 0;
					nodesValues[flatIdx] = 0.0d;
					nodesTrueNodeIds[flatIdx] = 0;
					nodesFalseNodeIds[flatIdx] = 0;
					nodesMissingValueTracksTrue[flatIdx] = 0;

					leafTreeIds[leafOffset + leafIdx] = treeIdx;
					leafNodeIds[leafOffset + leafIdx] = leafBaseNodeId + leafIdx;
					leafIds[leafOffset + leafIdx] = regressor ? 0 : treeIdx % numTreePerIteration;
					leafWeights[leafOffset + leafIdx] = tree.leafValue[leafIdx];
				}

				nodeOffset += numInternalNodes + tree.numLeaves;
				leafOffset += tree.numLeaves;
			}

			final Grove grove;
			if (regressor) {
				final RegressorGrove regressorGrove = new RegressorGrove();
				regressorGrove.targetTreeIds = leafTreeIds;
				regressorGrove.targetNodeIds = leafNodeIds;
				regressorGrove.targetIds = leafIds;
				regressorGrove.targetWeights = leafWeights;
				regressorGrove.nTargets = 1;
				regressorGrove.aggregateFunction = PetrifyConstants.AGGREGATE_SUM;
				grove = regressorGrove;
			} else {
				final ClassifierGrove classifierGrove = new ClassifierGrove();
				classifierGrove.classTreeIds = leafTreeIds;
				classifierGrove.classNodeIds = leafNodeIds;
				classifierGrove.classIds = leafIds;
				classifierGrove.classWeights = leafWeights;
				classifierGrove.classLabelsInt64s = buildClassLabels();
				grove = classifierGrove;
			}

			grove.nodesTreeIds = nodesTreeIds;
			grove.nodesNodeIds = nodesNodeIds;
			grove.nodesModes = nodesModes;
			grove.nodesFeatureIds = nodesFeatureIds;
			grove.nodesValues = nodesValues;
			grove.nodesTrueNodeIds = nodesTrueNodeIds;
			grove.nodesFalseNodeIds = nodesFalseNodeIds;
			grove.nodesHitRates = nodesHitRates;
			grove.nodesMissingValueTracksTrue = nodesMissingValueTracksTrue;
			grove.postTransform = postTransform;
			grove.baseValues = new double[] { 0.0d };
			grove.precisionMode = PrecisionMode.F64;
			return (T) grove;
		}

		protected int remapChild(final int childValue, final int leafBaseNodeId) {
			final int result;
			if (childValue >= 0) {
				result = childValue;
			} else {
				result = leafBaseNodeId + -childValue - 1;
			}
			return result;
		}

		protected long[] buildClassLabels() {
			final long[] labels = new long[numTreePerIteration];
			for (int labelIdx = 0; labelIdx < numTreePerIteration; labelIdx++) {
				labels[labelIdx] = labelIdx;
			}
			return labels;
		}

		protected String[] split(final String line) {
			return line.split("=", 2);
		}

		protected int[] parseIntArray(final String value) {
			final String[] parts = value.split(" ");
			final int[] result = new int[parts.length];
			for (int partIdx = 0; partIdx < parts.length; partIdx++) {
				result[partIdx] = Integer.parseInt(parts[partIdx]);
			}
			return result;
		}

		protected double parseDouble(final String value) {
			final double result;
			if (value.equals("inf")) {
				result = Double.POSITIVE_INFINITY;
			} else if (value.equals("-inf")) {
				result = Double.NEGATIVE_INFINITY;
			} else {
				result = Double.parseDouble(value);
			}
			return result;
		}

		protected double[] parseDoubleArray(final String value) {
			final String[] parts = value.split(" ");
			final double[] result = new double[parts.length];
			for (int partIdx = 0; partIdx < parts.length; partIdx++) {
				result[partIdx] = parseDouble(parts[partIdx]);
			}
			return result;
		}

		// line handling methods

		protected void magic(final String line) {
			if (line.equals(MARKER_MAGIC)) {
				state = ParseState.HEADER;
			} else {
				throw new UnexpectedTreeBranch("Expected LightGBM model magic 'tree', got: " + line);
			}
		}

		protected void headerVersion(final String header) {
			if (!header.equals(VALUE_V4)) {
				throw new UnexpectedTreeBranch("Unexpected LightGBM 'version'; only v4 supported, got: " + header);
			}
		}

		protected void headerObjective(final String objective) {
			final String objectiveName = objective.split(" ")[0];
			switch (objectiveName) {
				case OBJECTIVE_REGRESSION, OBJECTIVE_REGRESSION_L2, OBJECTIVE_MSE, OBJECTIVE_MSE_SHORT, //
						OBJECTIVE_RMSE, OBJECTIVE_RMSE_SHORT, OBJECTIVE_REGRESSION_L1, OBJECTIVE_MAE, OBJECTIVE_MAE_SHORT, //
						OBJECTIVE_HUBER, OBJECTIVE_FAIR, OBJECTIVE_POISSON, OBJECTIVE_QUANTILE, OBJECTIVE_MAPE, //
						OBJECTIVE_GAMMA, OBJECTIVE_TWEEDIE -> {
					regressor = true;
					postTransform = PetrifyConstants.POST_TRANSFORM_NONE;
				}
				case OBJECTIVE_BINARY, OBJECTIVE_CROSS_ENTROPY, OBJECTIVE_CROSS_ENTROPY_LAMBDA -> {
					regressor = false;
					postTransform = PetrifyConstants.POST_TRANSFORM_LOGISTIC;
				}
				case OBJECTIVE_MULTICLASS, OBJECTIVE_SOFTMAX, OBJECTIVE_MULTICLASSOVA, //
						OBJECTIVE_MULTICLASS_OVA, OBJECTIVE_OVA, OBJECTIVE_OVR -> {
					regressor = false;
					postTransform = PetrifyConstants.POST_TRANSFORM_SOFTMAX;
				}
				default -> throw new UnexpectedTreeBranch("Unsupported LightGBM 'objective': " + objective);
			}
		}

		protected void headerNumTreePerIteration(final String value) {
			numTreePerIteration = Integer.parseInt(value);
		}

		protected static class Tree {
			int numLeaves;
			int[] splitFeature;
			double[] threshold;
			int[] decisionType;
			int[] leftChild;
			int[] rightChild;
			double[] leafValue;
		}
	}
}
