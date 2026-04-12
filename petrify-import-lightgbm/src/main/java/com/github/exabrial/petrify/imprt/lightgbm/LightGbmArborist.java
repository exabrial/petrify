package com.github.exabrial.petrify.imprt.lightgbm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.github.exabrial.petrify.compiler.model.Grove;
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
			// TODO
		}

		protected void featureImportanceLine(final String line) {
			// TODO
		}

		protected void parameterLine(final String line) {
			// TODO
		}

		protected <T extends Grove> T buildGrove() {
			// TODO Auto-generated method stub
			return null;
		}

		protected String[] split(final String line) {
			return line.split("=");
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
			switch (objective) {
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
			double shrinkage;
		}
	}
}
