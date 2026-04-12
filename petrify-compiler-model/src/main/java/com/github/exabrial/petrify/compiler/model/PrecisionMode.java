package com.github.exabrial.petrify.compiler.model;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;

public enum PrecisionMode {
	F32 {
		@Override
		public void aload(final CodeBuilder codeBuilder) {
			codeBuilder.faload();
		}

		@Override
		public void astore(final CodeBuilder codeBuilder) {
			codeBuilder.fastore();
		}

		@Override
		public void mul(final CodeBuilder codeBuilder) {
			codeBuilder.fmul();
		}

		@Override
		public void add(final CodeBuilder codeBuilder) {
			codeBuilder.fadd();
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
		public void return_(final CodeBuilder codeBuilder) {
			codeBuilder.freturn();
		}

		@Override
		public void ldc(final CodeBuilder codeBuilder, final double value) {
			codeBuilder.ldc((float) value);
		}

		@Override
		public TypeKind typeKind() {
			return TypeKind.FLOAT;
		}

		@Override
		public ClassDesc scalarDesc() {
			return ConstantDescs.CD_float;
		}

		@Override
		public ClassDesc arrayDesc() {
			return ConstantDescs.CD_float.arrayType();
		}
	},

	F64 {
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
	};

	public abstract void aload(CodeBuilder codeBuilder);

	public abstract void astore(CodeBuilder codeBuilder);

	public abstract void mul(CodeBuilder codeBuilder);

	public abstract void add(CodeBuilder codeBuilder);

	public abstract void cmpg(CodeBuilder codeBuilder);

	public abstract void cmpl(CodeBuilder codeBuilder);

	public abstract void return_(CodeBuilder codeBuilder);

	public abstract void ldc(CodeBuilder codeBuilder, double value);

	public abstract TypeKind typeKind();

	public abstract ClassDesc scalarDesc();

	public abstract ClassDesc arrayDesc();
}
