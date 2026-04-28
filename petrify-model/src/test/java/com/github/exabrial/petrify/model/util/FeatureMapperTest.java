/*
 * Licensed under the terms of Apache Source License 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.github.exabrial.petrify.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import com.github.exabrial.petrify.model.Fossil;
import com.github.exabrial.petrify.model.exception.FeatureUnconformity;
import com.github.exabrial.petrify.model.exception.FossilUnconformity;

@ExtendWith(MockitoExtension.class)
class FeatureMapperTest {
	private static final List<String> FEATURE_NAMES = List.of("feat_0", "feat_1", "feat_2");

	private FeatureMapper mapper;

	@Mock
	private Logger mockLog;

	private final Fossil fossil = new Fossil() {
		private static final long serialVersionUID = 1L;

		@Override
		public List<String> getFeatureNames() {
			return FEATURE_NAMES;
		}
	};

	@BeforeEach
	void beforeEach() throws Exception {
		mapper = new FeatureMapper(fossil);
		final Field logField = FeatureMapper.class.getDeclaredField("log");
		logField.setAccessible(true);
		logField.set(mapper, mockLog);
	}

	@Test
	void testConstructor_fossilWithEmptyFeatureNamesThrows() {
		final Fossil emptyFossil = new Fossil() {
			private static final long serialVersionUID = 1L;
		};
		assertThrows(FossilUnconformity.class, () -> new FeatureMapper(emptyFossil));
	}

	@Test
	void testConstructor_listAndFossil() {
		final List<String> names = List.of("a", "b");
		final FeatureMapper listMapper = new FeatureMapper(names, fossil);

		final Map<String, Object> features = new HashMap<>();
		features.put("a", 1.0);
		features.put("b", 2.0);

		final double[] array = listMapper.mapToF64(features);
		assertEquals(1.0, array[0]);
		assertEquals(2.0, array[1]);
	}

	@Test
	void testConstructor_emptyListThrows() {
		assertThrows(FossilUnconformity.class, () -> new FeatureMapper(List.of(), fossil));
	}

	@Test
	void testmapToF64_allFeaturesPresent() {
		final Map<String, Object> features = new HashMap<>();
		features.put("feat_0", 1.0);
		features.put("feat_1", 2.0);
		features.put("feat_2", 3.0);

		final double[] array = mapper.mapToF64(features);
		assertEquals(3, array.length);
		assertEquals(1.0, array[0]);
		assertEquals(2.0, array[1]);
		assertEquals(3.0, array[2]);
		verifyNoInteractions(mockLog);
	}

	@Test
	void testmapToF64_missingKeyBecomesNaN() {
		final Map<String, Object> features = new HashMap<>();
		features.put("feat_0", 1.0);

		final double[] array = mapper.mapToF64(features);
		assertEquals(1.0, array[0]);
		assertTrue(Double.isNaN(array[1]));
		assertTrue(Double.isNaN(array[2]));
		verify(mockLog).warn(any(String.class), eq(mapper.mappedFor), eq(2), eq(List.of("feat_1", "feat_2")));
	}

	@Test
	void testmapToF64_missingKeyThrowsWhenEnabled() {
		mapper.setTossExceptionOnMissingFeatures(true);
		final Map<String, Object> features = new HashMap<>();
		features.put("feat_0", 1.0);

		assertThrows(FeatureUnconformity.class, () -> mapper.mapToF64(features));
		verify(mockLog).warn(any(String.class), eq(mapper.mappedFor), eq(2), eq(List.of("feat_1", "feat_2")));
	}

	@Test
	void testmapToF64_integerConversion() {
		final Map<String, Object> features = new HashMap<>();
		features.put("feat_0", 5);

		final double[] array = mapper.mapToF64(features);
		assertEquals(5.0, array[0]);
	}

	@Test
	void testmapToF64_booleanConversion() {
		final Map<String, Object> features = new HashMap<>();
		features.put("feat_0", true);
		features.put("feat_1", false);

		final double[] array = mapper.mapToF64(features);
		assertEquals(1.0d, array[0]);
		assertEquals(0.0d, array[1]);
	}

	@Test
	void testmapToF64_unsupportedTypeThrows() {
		final Map<String, Object> features = new HashMap<>();
		features.put("feat_0", "not_a_number");

		assertThrows(FossilUnconformity.class, () -> mapper.mapToF64(features));
	}

	@Test
	void testmapToF32_allFeaturesPresent() {
		final Map<String, Object> features = new HashMap<>();
		features.put("feat_0", 1.0);
		features.put("feat_1", 2.0);
		features.put("feat_2", 3.0);

		final float[] array = mapper.mapToF32(features);
		assertEquals(3, array.length);
		assertEquals(1.0f, array[0]);
		assertEquals(2.0f, array[1]);
		assertEquals(3.0f, array[2]);
		verifyNoInteractions(mockLog);
	}

	@Test
	void testmapToF32_missingKeyBecomesNaN() {
		final Map<String, Object> features = new HashMap<>();
		features.put("feat_0", 1.0f);

		final float[] array = mapper.mapToF32(features);
		assertEquals(1.0f, array[0]);
		assertTrue(Float.isNaN(array[1]));
		assertTrue(Float.isNaN(array[2]));
		verify(mockLog).warn(any(String.class), eq(mapper.mappedFor), eq(2), eq(List.of("feat_1", "feat_2")));
	}

	@Test
	void testmapToF32_missingKeyThrowsWhenEnabled() {
		mapper.setTossExceptionOnMissingFeatures(true);
		final Map<String, Object> features = new HashMap<>();
		features.put("feat_0", 1.0);

		assertThrows(FeatureUnconformity.class, () -> mapper.mapToF32(features));
		verify(mockLog).warn(any(String.class), eq(mapper.mappedFor), eq(2), eq(List.of("feat_1", "feat_2")));
	}

	@Test
	void testmapToF32_booleanConversion() {
		final Map<String, Object> features = new HashMap<>();
		features.put("feat_0", true);
		features.put("feat_1", false);

		final float[] array = mapper.mapToF32(features);
		assertEquals(1.0f, array[0]);
		assertEquals(0.0f, array[1]);
	}

	@Test
	void testmapToF32_unsupportedTypeThrows() {
		final Map<String, Object> features = new HashMap<>();
		features.put("feat_0", "not_a_number");

		assertThrows(FossilUnconformity.class, () -> mapper.mapToF32(features));
	}

	@Test
	void testToString() {
		final String result = mapper.toString();
		assertTrue(result.contains("mappedFor="));
		assertTrue(result.contains("featureCount=3"));
		assertTrue(result.contains("tossExceptionOnMissingFeatures=false"));
	}
}
