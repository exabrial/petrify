package com.github.exabrial.petrify.imprt;

import com.github.exabrial.petrify.compiler.model.Grove;

public interface Arborist {
	<T extends Grove> T toGrove(String classpathLocation);

	<T extends Grove> T toGrove(byte[] bytes);
}