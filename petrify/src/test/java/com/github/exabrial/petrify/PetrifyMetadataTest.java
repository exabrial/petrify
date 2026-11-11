package com.github.exabrial.petrify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.compiler.model.ModelMetadata;
import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.compiler.model.RegressorGrove;
import com.github.exabrial.petrify.model.PetrifyConstants;
import com.github.exabrial.petrify.model.RegressionFossil;
import com.github.exabrial.petrify.testing.GroveGenerator;

class PetrifyMetadataTest {
	private final GroveGenerator groveGenerator = new GroveGenerator(PrecisionMode.F64);
	private final Petrify petrify = new Petrify();

	@Test
	void test_metadata() throws Exception {
		final RegressorGrove grove = groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_EQ);
		grove.metadata = new ModelMetadata();
		grove.metadata.modelFamily = "geology";
		grove.metadata.modelName = "volcanic_risk";
		grove.metadata.majorVersion = 2;
		grove.metadata.minorVersion = 1;
		grove.metadata.featureNames = new String[] { "temperature", "pressure" };

		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);
		assertEquals("geology", fossil.getModelFamily());
		assertEquals("volcanic_risk", fossil.getModelName());
		assertEquals(2, fossil.getMajorVersion());
		assertEquals(1, fossil.getMinorVersion());
		assertEquals("2.1", fossil.getModelVersion());
		assertEquals(List.of("temperature", "pressure"), fossil.getFeatureNames());
	}

	@Test
	void test_metadata_noVersion() throws Exception {
		final RegressorGrove grove = groveGenerator.singleSplitRegressorGrove(PetrifyConstants.MODE_BRANCH_EQ);
		grove.metadata = new ModelMetadata();
		grove.metadata.modelName = "volcanic_risk";

		final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);
		assertEquals("volcanic_risk", fossil.getModelName());
		assertNull(fossil.getModelFamily());
		assertNull(fossil.getMajorVersion());
		assertNull(fossil.getMinorVersion());
		assertNull(fossil.getModelVersion());
	}
}
