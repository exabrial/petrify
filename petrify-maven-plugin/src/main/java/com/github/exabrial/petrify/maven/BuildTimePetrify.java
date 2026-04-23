package com.github.exabrial.petrify.maven;

import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.exabrial.petrify.CompiledModel;
import com.github.exabrial.petrify.Petrify;
import com.github.exabrial.petrify.model.Fossil;

public class BuildTimePetrify extends Petrify {
	private ClassDesc classDesc;
	private CompiledModel fossilClass;
	private final List<CompiledModel> innerClasses = new ArrayList<>();

	public void setTarget(final String packageName, final String className) {
		classDesc = ClassDesc.of(packageName, className);
	}

	public CompiledModel getFossilClass() {
		return fossilClass;
	}

	public List<CompiledModel> getInnerClasses() {
		return Collections.unmodifiableList(innerClasses);
	}

	@Override
	protected ClassDesc nextClassDesc(final MethodHandles.Lookup lookup) {
		return classDesc;
	}

	@Override
	protected <T extends Fossil> T defineFossil(final MethodHandles.Lookup lookup, final CompiledModel compiledClass,
			final Class<T> fossilType) {
		this.fossilClass = compiledClass;
		return null;
	}

	@Override
	protected void defineInnerClass(final MethodHandles.Lookup lookup, final CompiledModel compiledClass) {
		this.innerClasses.add(compiledClass);
	}
}
