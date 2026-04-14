package com.github.exabrial.petrify.maven;

import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandles;

import com.github.exabrial.petrify.Petrify;
import com.github.exabrial.petrify.model.Fossil;

public class BuildTimePetrify extends Petrify {
	private ClassDesc classDesc;
	private byte[] fossilBytes;

	public void setTarget(final String packageName, final String className) {
		classDesc = ClassDesc.of(packageName, className);
	}

	public byte[] getFossilBytes() {
		return fossilBytes;
	}

	@Override
	protected ClassDesc nextClassDesc(final MethodHandles.Lookup lookup) {
		return classDesc;
	}

	@Override
	protected <T extends Fossil> T defineFossil(final MethodHandles.Lookup lookup, final byte[] fossilBytes,
			final Class<T> fossilType) {
		this.fossilBytes = fossilBytes;
		return null;
	}
}
