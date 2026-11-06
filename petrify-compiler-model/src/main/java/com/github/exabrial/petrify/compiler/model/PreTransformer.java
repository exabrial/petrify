package com.github.exabrial.petrify.compiler.model;

import java.io.Serializable;

/**
 * One step applied to a single input feature before the model runs. Steps for a feature run in list order; each receives the output of
 * the one before it.
 */
public record PreTransformer(PreTransformerType type, double value) implements Serializable {
}
