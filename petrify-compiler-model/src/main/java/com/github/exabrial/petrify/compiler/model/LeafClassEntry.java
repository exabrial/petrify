package com.github.exabrial.petrify.compiler.model;

/**
 * One contribution of a classifier leaf: when the tree reaches the leaf, {@code weight} is added to the score of {@code classId}.
 */
public record LeafClassEntry(int classId, double weight) {
}
