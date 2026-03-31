package com.github.exabrial.petrify.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.Petrify;
import com.github.exabrial.petrify.imprt.Arborist;
import com.github.exabrial.petrify.model.Fossil;
import com.github.exabrial.petrify.model.Grove;

/**
 * Uses a LightGBM-exported ONNX model trained on California housing data binned into 3 price tiers (low/medium/high). 90 trees (30
 * estimators x 3 classes), max_depth=6, num_leaves=31, BRANCH_LEQ node mode, post_transform=SOFTMAX, 8 input features (MedInc,
 * HouseAge, AveRooms, AveBedrms, Population, AveOccup, Latitude, Longitude). 5,364 total nodes.
 */
class LightgbmHousingTierTest {

	@Test
	void testLightgbmHousingTier() {
		final Arborist arborist = new Arborist();
		final Grove grove = arborist.toGrove("/test-models/lightgbmHousingTier.onnx");

		final Petrify petrify = new Petrify();
		final Fossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);

		// Class 0 (low price tier) predictions
		assertEquals(0, fossil.predict(new float[] { 5.555099964141846f, 9.0f, 6.709506988525391f, 1.0651408433914185f, 1518.0f,
				2.6725351810455322f, 35.54999923706055f, -117.68000030517578f }));
		assertEquals(0, fossil.predict(new float[] { 3.65910005569458f, 52.0f, 5.67307710647583f, 0.9153845906257629f, 736.0f,
				2.8307693004608154f, 37.959999084472656f, -121.30000305175781f }));
		assertEquals(0, fossil.predict(new float[] { 0.8639000058174133f, 28.0f, 4.289377212524414f, 1.0952380895614624f, 1193.0f,
				4.3699631690979f, 35.380001068115234f, -118.9800033569336f }));

		// Class 1 (medium price tier) predictions
		assertEquals(1, fossil.predict(new float[] { 5.463900089263916f, 35.0f, 6.057534217834473f, 0.9589040875434875f, 1004.0f,
				2.750684976577759f, 37.709999084472656f, -122.08000183105469f }));
		assertEquals(1, fossil.predict(new float[] { 3.3905999660491943f, 15.0f, 4.023611068725586f, 1.0111111402511597f, 2340.0f, 3.25f,
				37.66999816894531f, -122.4800033569336f }));
		assertEquals(1, fossil.predict(new float[] { 5.793399810791016f, 27.0f, 7.250504970550537f, 1.0242424011230469f, 1484.0f,
				2.9979798793792725f, 33.779998779296875f, -117.80999755859375f }));

		// Class 2 (high price tier) predictions
		assertEquals(2, fossil.predict(new float[] { 3.625f, 45.0f, 4.9178080558776855f, 1.2808219194412231f, 662.0f, 2.267123222351074f,
				33.959999084472656f, -118.38999938964844f }));
		assertEquals(2, fossil.predict(new float[] { 9.795599937438965f, 27.0f, 7.988188743591309f, 0.9527559280395508f, 711.0f,
				2.799212694168091f, 33.900001525878906f, -117.94000244140625f }));
		assertEquals(2, fossil.predict(new float[] { 7.624499797821045f, 21.0f, 6.663822650909424f, 0.9948805570602417f, 1528.0f,
				2.607508420944214f, 36.54999923706055f, -121.69999694824219f }));

		// Highest confidence predictions
		assertEquals(1, fossil.predict(new float[] { 4.505199909210205f, 32.0f, 4.636363506317139f, 0.9220778942108154f, 654.0f,
				4.246753215789795f, 33.77000045776367f, -117.94000244140625f }));
		assertEquals(2, fossil.predict(new float[] { 8.469300270080566f, 26.0f, 6.993079662322998f, 1.0224913358688354f, 1611.0f,
				2.7871971130371094f, 37.52000045776367f, -122.31999969482422f }));
	}
}
