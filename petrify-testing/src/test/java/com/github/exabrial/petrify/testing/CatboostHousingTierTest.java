package com.github.exabrial.petrify.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.Petrify;
import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
import com.github.exabrial.petrify.imprt.Arborist;
import com.github.exabrial.petrify.model.ClassifierFossil;

/**
 * Uses a CatBoost-exported ONNX model trained on California housing data binned into 3 price tiers (low/medium/high). 30 iterations,
 * depth=6, learning_rate=0.1, MultiClass loss, BRANCH_GT node mode (CatBoost symmetric/oblivious trees), post_transform=SOFTMAX, 8
 * input features (MedInc, HouseAge, AveRooms, AveBedrms, Population, AveOccup, Latitude, Longitude). 3,810 total nodes (1,890 branch +
 * 1,920 leaf).
 */
class CatboostHousingTierTest {

	@Test
	void testCatboostHousingTier() {
		final Arborist arborist = new Arborist();
		final ClassifierGrove grove = arborist.toGrove(ClassifierGrove.class, "/test-models/catboostHousingTier.onnx");

		final Petrify petrify = new Petrify();
		final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);

		// Class 0 (low price tier) predictions
		assertEquals(0, fossil.predict(new float[] { 2.125f, 31.0f, 4.818181991577148f, 1.0f, 186.0f, 3.3818182945251465f,
				34.47999954223633f, -117.1500015258789f }));
		assertEquals(0, fossil.predict(new float[] { 1.399999976158142f, 20.0f, 5.9301533699035645f, 1.4310051202774048f, 1694.0f,
				2.8858602046966553f, 34.52000045776367f, -116.9000015258789f }));
		assertEquals(0, fossil.predict(new float[] { 2.178100109100342f, 10.0f, 5.1992573738098145f, 1.0519802570343018f, 2378.0f,
				2.9430692195892334f, 34.5f, -117.19999694824219f }));

		// Class 1 (medium price tier) predictions
		assertEquals(1, fossil.predict(new float[] { 4.267899990081787f, 25.0f, 4.941999912261963f, 1.0360000133514404f, 1539.0f,
				3.078000068664551f, 33.84000015258789f, -117.97000122070312f }));
		assertEquals(1, fossil.predict(new float[] { 4.36359977722168f, 33.0f, 5.510204315185547f, 1.0148422718048096f, 1869.0f,
				3.4675323963165283f, 33.810001373291016f, -118.0f }));
		assertEquals(1, fossil.predict(new float[] { 4.777200222015381f, 34.0f, 4.985915660858154f, 0.9753521084785461f, 980.0f,
				3.450704336166382f, 33.810001373291016f, -117.95999908447266f }));

		// Class 2 (high price tier) predictions
		assertEquals(2, fossil.predict(new float[] { 10.923700332641602f, 44.0f, 7.837963104248047f, 1.1064814329147339f, 498.0f,
				2.305555582046509f, 34.13999938964844f, -118.43000030517578f }));
		assertEquals(2, fossil.predict(new float[] { 8.152999877929688f, 36.0f, 7.580474853515625f, 1.1081794500350952f, 850.0f,
				2.242743968963623f, 34.13999938964844f, -118.47000122070312f }));
		assertEquals(2, fossil.predict(new float[] { 12.848299980163574f, 42.0f, 6.831578731536865f, 0.9157894849777222f, 420.0f,
				2.21052622795105f, 34.040000915527344f, -118.4000015258789f }));
	}
}
