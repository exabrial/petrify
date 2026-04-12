package com.github.exabrial.petrify.imprt;

import com.github.exabrial.petrify.compiler.model.Vine;

public interface Vinter {
	<T extends Vine> T toVine(String classpathLocation);

	<T extends Vine> T toVine(byte[] bytes);
}