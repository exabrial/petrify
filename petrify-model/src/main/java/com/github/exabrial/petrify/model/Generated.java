/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or method as produced by the Petrify bytecode compiler.
 *
 * <p>
 * Uses {@link RetentionPolicy#CLASS} so the annotation is written into the generated class file but incurs no runtime overhead. Tools
 * like JaCoCo recognize any annotation whose simple name is {@code Generated} and exclude the annotated elements from coverage
 * reports.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Generated {

	String value() default "petrify";
}
