package com.github.exabrial.petrify.compiler.model;

/**
 * The operation a {@link PreTransformer} applies to one input feature. A {@code NaN} input stays {@code NaN} for every type except
 * {@code fill}.
 */
public enum PreTransformerType {
	/**
	 * Does nothing. Pass-through.
	 */
	noop,

	/**
	 * Replaces a missing input ({@code NaN}) with {@code value}; any other input passes through unchanged.
	 */
	fill,

	/**
	 * Upper bound: the output never exceeds {@code value}. Computes {@code Math.min(input, value)}; same as {@code a_max} in
	 * {@code np.clip}.
	 */
	max,

	/**
	 * Lower bound: the output is never below {@code value}. Computes {@code Math.max(input, value)}; same as {@code a_min} in
	 * {@code np.clip}.
	 */
	min,

	/**
	 * Computes {@code Math.log1p(input)}, which is {@code ln(1 + input)}; same as {@code np.log1p}. Has no {@code value}.
	 */
	log1p;
}
