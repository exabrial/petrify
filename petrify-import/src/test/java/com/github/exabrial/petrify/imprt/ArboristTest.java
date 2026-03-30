package com.github.exabrial.petrify.imprt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.exabrial.petrify.model.Grove;
import com.github.exabrial.petrify.model.exception.UnexpectedCometImpact;
import com.google.protobuf.ByteString;

import onnx.OnnxMl.AttributeProto;
import onnx.OnnxMl.GraphProto;
import onnx.OnnxMl.NodeProto;

class ArboristTest {
	private Arborist arborist;

	@BeforeEach
	void setUp() {
		arborist = new Arborist();
	}

	@Test
	void testMapToGrove() {
		final NodeProto treeNode = buildTreeEnsembleNode();
		final Grove grove = arborist.mapToGrove(treeNode);

		assertNotNull(grove);
		assertArrayEquals(new int[] { 0, 0, 0 }, grove.getNodesTreeIds());
		assertArrayEquals(new int[] { 0, 1, 2 }, grove.getNodesNodeIds());
		assertArrayEquals(new byte[] { 1, 0, 0 }, grove.getNodesModes());
		assertArrayEquals(new int[] { 0, 0, 0 }, grove.getNodesFeatureIds());
		assertArrayEquals(new float[] { 4.0f, 0.0f, 0.0f }, grove.getNodesValues());
		assertArrayEquals(new int[] { 1, 0, 0 }, grove.getNodesTrueNodeIds());
		assertArrayEquals(new int[] { 2, 0, 0 }, grove.getNodesFalseNodeIds());
		assertArrayEquals(new float[] { 1.0f, 1.0f, 1.0f }, grove.getNodesHitRates());
		assertArrayEquals(new int[] { 0, 0, 0 }, grove.getNodesMissingValueTracksTrue());
		assertArrayEquals(new int[] { 0, 0 }, grove.getClassTreeIds());
		assertArrayEquals(new int[] { 1, 2 }, grove.getClassNodeIds());
		assertArrayEquals(new int[] { 0, 1 }, grove.getClassIds());
		assertArrayEquals(new float[] { 1.0f, 1.0f }, grove.getClassWeights());
		assertArrayEquals(new long[] { 0L, 1L }, grove.getClassLabelsInt64s());
		assertEquals((byte) 0, grove.getPostTransform());
	}

	@Test
	void testFindTreeEnsembleNode() {
		final NodeProto treeNode = buildTreeEnsembleNode();
		final GraphProto graph = GraphProto.newBuilder().addNode(treeNode).build();

		final NodeProto found = arborist.findTreeEnsembleNode(graph);
		assertEquals("TreeEnsembleClassifier", found.getOpType());
	}

	@Test
	void testFindTreeEnsembleNodeThrowsWhenNotFound() {
		final GraphProto graph = GraphProto.newBuilder().addNode(NodeProto.newBuilder().setOpType("Conv").build()).build();

		assertThrows(UnexpectedCometImpact.class, () -> arborist.findTreeEnsembleNode(graph));
	}

	@Test
	void testLoadModelThrowsOnMissingResource() {
		assertThrows(UnexpectedCometImpact.class, () -> arborist.loadModel("nonexistent.onnx"));
	}

	@Test
	void testUnknownModeThrows() {
		assertThrows(UnexpectedCometImpact.class, () -> arborist.toModeByte("UNKNOWN_MODE"));
	}

	private NodeProto buildTreeEnsembleNode() {
		return NodeProto.newBuilder().setOpType("TreeEnsembleClassifier").addAttribute(intsAttr("nodes_treeids", 0, 0, 0))
				.addAttribute(intsAttr("nodes_nodeids", 0, 1, 2)).addAttribute(stringsAttr("nodes_modes", "BRANCH_LEQ", "LEAF", "LEAF"))
				.addAttribute(intsAttr("nodes_featureids", 0, 0, 0)).addAttribute(floatsAttr("nodes_values", 4.0f, 0.0f, 0.0f))
				.addAttribute(intsAttr("nodes_truenodeids", 1, 0, 0)).addAttribute(intsAttr("nodes_falsenodeids", 2, 0, 0))
				.addAttribute(floatsAttr("nodes_hitrates", 1.0f, 1.0f, 1.0f))
				.addAttribute(intsAttr("nodes_missing_value_tracks_true", 0, 0, 0)).addAttribute(intsAttr("class_treeids", 0, 0))
				.addAttribute(intsAttr("class_nodeids", 1, 2)).addAttribute(intsAttr("class_ids", 0, 1))
				.addAttribute(floatsAttr("class_weights", 1.0f, 1.0f)).addAttribute(intsAttr("classlabels_int64s", 0, 1))
				.addAttribute(stringAttr("post_transform", "NONE")).build();
	}

	private AttributeProto intsAttr(final String name, final long... values) {
		final AttributeProto.Builder builder = AttributeProto.newBuilder().setName(name);
		for (final long v : values) {
			builder.addInts(v);
		}
		return builder.build();
	}

	private AttributeProto floatsAttr(final String name, final float... values) {
		final AttributeProto.Builder builder = AttributeProto.newBuilder().setName(name);
		for (final float v : values) {
			builder.addFloats(v);
		}
		return builder.build();
	}

	private AttributeProto stringsAttr(final String name, final String... values) {
		final AttributeProto.Builder builder = AttributeProto.newBuilder().setName(name);
		for (final String v : values) {
			builder.addStrings(ByteString.copyFromUtf8(v));
		}
		return builder.build();
	}

	private AttributeProto stringAttr(final String name, final String value) {
		return AttributeProto.newBuilder().setName(name).setS(ByteString.copyFromUtf8(value)).build();
	}
}
