package com.github.exabrial.petrify.internal.model;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;

public final class SinglePrecisionByteCodeAdapter implements ByteCodeAdapter {

	@Override
	public void add(final CodeBuilder codeBuilder) {
		codeBuilder.fadd();
	}

	@Override
	public void aload(final CodeBuilder codeBuilder) {
		codeBuilder.faload();
	}

	@Override
	public void astore(final CodeBuilder codeBuilder) {
		codeBuilder.fastore();
	}

	@Override
	public void cmpg(final CodeBuilder codeBuilder) {
		codeBuilder.fcmpg();
	}

	@Override
	public void cmpl(final CodeBuilder codeBuilder) {
		codeBuilder.fcmpl();
	}

	@Override
	public void ldc(final CodeBuilder codeBuilder, final double value) {
		codeBuilder.ldc((float) value);
	}

	@Override
	public void mul(final CodeBuilder codeBuilder) {
		codeBuilder.fmul();
	}

	@Override
	public void return_(final CodeBuilder codeBuilder) {
		codeBuilder.freturn();
	}

	@Override
	public ClassDesc arrayDesc() {
		return ConstantDescs.CD_float.arrayType();
	}

	@Override
	public ClassDesc scalarDesc() {
		return ConstantDescs.CD_float;
	}

	@Override
	public TypeKind typeKind() {
		return TypeKind.FLOAT;
	}
}
