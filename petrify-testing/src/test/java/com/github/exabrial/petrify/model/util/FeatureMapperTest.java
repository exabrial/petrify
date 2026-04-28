package com.github.exabrial.petrify.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.petrify.Petrify;
import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
import com.github.exabrial.petrify.imprt.Arborist;
import com.github.exabrial.petrify.imprt.lightgbm.LightGbmArborist;
import com.github.exabrial.petrify.model.ClassifierFossil;

@TestInstance(Lifecycle.PER_CLASS)
class FeatureMapperTest {
	private static final String MODEL = "/test-models/lightgbmHousingTier.txt";
	private static final int LOW = 0;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private ClassifierFossil fossil;
	private FeatureMapper mapper;

	@BeforeAll
	void beforeAll() {
		final Arborist arborist = new LightGbmArborist();
		final ClassifierGrove grove = arborist.toGrove(MODEL);

		final Petrify petrify = new Petrify();
		fossil = petrify.fossilize(MethodHandles.lookup(), grove);
		mapper = new FeatureMapper(fossil);
	}

	@BeforeEach
	void beforeEach(final TestInfo testInfo) {
		log.info("beforeEach() starting test:{}", testInfo.getDisplayName());
	}

	@Test
	void testmapToF64_predictMatchesRawArray() {
		final Map<String, Object> features = new HashMap<>();
		features.put("Column_0", 3.625);
		features.put("Column_1", 16.0);
		features.put("Column_2", 4.404833836858006);
		features.put("Column_3", 0.8912386706948641);
		features.put("Column_4", 912.0);
		features.put("Column_5", 2.7552870090634443);
		features.put("Column_6", 34.06);
		features.put("Column_7", -117.71);

		final double[] array = mapper.mapToF64(features);
		final int actual = fossil.predict(array);
		assertEquals(LOW, actual);
	}

	@Test
	void testmapToF64_extraKeysIgnored() {
		final Map<String, Object> features = new HashMap<>();
		features.put("Column_0", 3.625);
		features.put("Column_1", 16.0);
		features.put("Column_2", 4.404833836858006);
		features.put("Column_3", 0.8912386706948641);
		features.put("Column_4", 912.0);
		features.put("Column_5", 2.7552870090634443);
		features.put("Column_6", 34.06);
		features.put("Column_7", -117.71);
		features.put("extra_feature_not_in_model", 999.0);

		final double[] array = mapper.mapToF64(features);
		assertEquals(8, array.length);
	}
}
