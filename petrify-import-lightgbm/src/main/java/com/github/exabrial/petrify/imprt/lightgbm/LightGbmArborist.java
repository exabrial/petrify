package com.github.exabrial.petrify.imprt.lightgbm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import com.github.exabrial.petrify.compiler.model.Grove;
import com.github.exabrial.petrify.compiler.model.exception.MissingSpecimen;
import com.github.exabrial.petrify.compiler.model.exception.UnexpectedCometImpact;
import com.github.exabrial.petrify.imprt.Arborist;

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
		protected static final String MARKER_TREE_PREFIX = "Tree=";
		protected static final String MARKER_END_OF_TREES = "end of trees";
		protected static final String MARKER_FEATURE_IMPORTANCES = "feature_importances:";
		protected static final String MARKER_PARAMETERS = "parameters:";
		protected static final String MARKER_END_OF_PARAMETERS = "end of parameters";

		private ParseState state = ParseState.HEADER;

		enum ParseState {
			HEADER, TREE, FEATURE_IMPORTANCES, PARAMETERS, DONE
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
			// TODO
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
	}
}
