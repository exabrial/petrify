package com.github.exabrial.petrify.internal.model;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;

public interface ByteCodeAdapter {

	/**
	 * Generates an instruction to add two (float|double)s.
	 *
	 * @param codeBuilder
	 */
	void add(CodeBuilder codeBuilder);

	/**
	 * Generates an instruction to load from a (float|double) array.
	 *
	 * @param codeBuilder
	 */
	void aload(CodeBuilder codeBuilder);

	/**
	 * Generates an instruction to store into a (float|double) array.
	 *
	 * @param codeBuilder
	 */
	void astore(CodeBuilder codeBuilder);

	/**
	 * Generates an instruction to compare (float|double)s, producing {@code 1} if any operand is NaN.
	 *
	 * @param codeBuilder
	 */
	void cmpg(CodeBuilder codeBuilder);

	/**
	 * Generates an instruction to compare (float|double)s, producing {@code -1} if any operand is NaN.
	 *
	 * @param codeBuilder
	 */
	void cmpl(CodeBuilder codeBuilder);

	/**
	 * Generates an instruction pushing a constant (float|double) value onto the operand stack.
	 *
	 * @param codeBuilder
	 * @param value
	 *          the constant value to push
	 */
	void ldc(CodeBuilder codeBuilder, double value);

	/**
	 * Generates an instruction to multiply (float|double)s.
	 *
	 * @param codeBuilder
	 */
	void mul(CodeBuilder codeBuilder);

	/**
	 * Generates an instruction to return a (float|double) from this method.
	 *
	 * @param codeBuilder
	 */
	void return_(CodeBuilder codeBuilder);

	/**
	 * Returns the array {@link ClassDesc} for this adapter's precision ({@code float[]} or {@code double[]}).
	 *
	 * @return the array class descriptor
	 */
	ClassDesc arrayDesc();

	/**
	 * Returns the scalar {@link ClassDesc} for this adapter's precision ({@code float} or {@code double}).
	 *
	 * @return the scalar class descriptor
	 */
	ClassDesc scalarDesc();

	/**
	 * Returns the {@link TypeKind} for this adapter's precision ({@code FLOAT} or {@code DOUBLE}).
	 *
	 * @return the type kind
	 */
	TypeKind typeKind();
}
