package com.github.exabrial.petrify.compiler.model;

/**
 * One contribution of a regressor leaf: when the tree reaches the leaf, {@code weight} is added to the score of {@code targetId}.
 */
public record LeafTargetEntry(int targetId, double weight) {
}
