package com.github.exabrial.petrify.internal.model;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;

public interface ByteCodeAdapter {

	void aload(CodeBuilder codeBuilder);

	void astore(CodeBuilder codeBuilder);

	void mul(CodeBuilder codeBuilder);

	void add(CodeBuilder codeBuilder);

	void cmpg(CodeBuilder codeBuilder);

	void cmpl(CodeBuilder codeBuilder);

	void return_(CodeBuilder codeBuilder);

	void ldc(CodeBuilder codeBuilder, double value);

	TypeKind typeKind();

	ClassDesc scalarDesc();

	ClassDesc arrayDesc();
}
