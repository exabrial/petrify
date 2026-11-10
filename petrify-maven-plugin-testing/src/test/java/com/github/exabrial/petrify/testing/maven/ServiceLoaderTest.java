package com.github.exabrial.petrify.testing.maven;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.model.ClassifierFossil;
import com.github.exabrial.petrify.model.Fossil;

class ServiceLoaderTest {

	@Test
	void testFossilServiceLoad() {
		final ServiceLoader<Fossil> serviceLoader = ServiceLoader.load(Fossil.class);
		final Provider<Fossil> provider = serviceLoader.stream().findFirst().get();
		final Fossil fossil = provider.get();
		assertInstanceOf(DecisionTreeSimpleFossil.class, fossil);
	}

	@Test
	void testClassifierFossilServiceLoad() {
		final ServiceLoader<ClassifierFossil> serviceLoader = ServiceLoader.load(ClassifierFossil.class);
		final Provider<ClassifierFossil> provider = serviceLoader.stream().findFirst().get();
		final ClassifierFossil fossil = provider.get();
		assertInstanceOf(DecisionTreeSimpleFossil.class, fossil);
	}
}
