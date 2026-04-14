package com.github.exabrial.petrify.internal.model;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;

public final class DoublePrecisionByteCodeAdapter implements ByteCodeAdapter {

	@Override
	public void aload(final CodeBuilder codeBuilder) {
		codeBuilder.daload();
	}

	@Override
	public void astore(final CodeBuilder codeBuilder) {
		codeBuilder.dastore();
	}

	@Override
	public void mul(final CodeBuilder codeBuilder) {
		codeBuilder.dmul();
	}

	@Override
	public void add(final CodeBuilder codeBuilder) {
		codeBuilder.dadd();
	}

	@Override
	public void cmpg(final CodeBuilder codeBuilder) {
		codeBuilder.dcmpg();
	}

	@Override
	public void cmpl(final CodeBuilder codeBuilder) {
		codeBuilder.dcmpl();
	}

	@Override
	public void return_(final CodeBuilder codeBuilder) {
		codeBuilder.dreturn();
	}

	@Override
	public void ldc(final CodeBuilder codeBuilder, final double value) {
		codeBuilder.ldc(value);
	}

	@Override
	public TypeKind typeKind() {
		return TypeKind.DOUBLE;
	}

	@Override
	public ClassDesc scalarDesc() {
		return ConstantDescs.CD_double;
	}

	@Override
	public ClassDesc arrayDesc() {
		return ConstantDescs.CD_double.arrayType();
	}
}
