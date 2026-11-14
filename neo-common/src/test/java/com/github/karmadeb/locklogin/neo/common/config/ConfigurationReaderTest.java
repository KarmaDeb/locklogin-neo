package com.github.karmadeb.locklogin.neo.common.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationReaderTest {

	private MappingNode createNode(String key, String value) {
		MappingNode mapping = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, key, null, null, ScalarStyle.PLAIN);
		ScalarNode valueNode = new ScalarNode(Tag.STR, value, null, null, ScalarStyle.SINGLE_QUOTED);
		mapping.getValue().add(new NodeTuple(keyNode, valueNode));
		return mapping;
	}

	private ConfigurationReader createReader() {
		return new ConfigurationReader(new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO));
	}

	// --- getString tests ---

	@Test
	void getString_existingKey_returnsValue() {
		MappingNode node = createNode("name", "Hello");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<String> result = reader.getString("name");
		assertTrue(result.isPresent());
		assertEquals("Hello", result.get());
	}

	@Test
	void getString_missingKey_returnsEmpty() {
		ConfigurationReader reader = createReader();

		Optional<String> result = reader.getString("missing");
		// resolve returns Optional.empty(), so getString returns empty
		assertTrue(result.isEmpty());
	}

	// --- getInt tests ---

	@Test
	void getInt_validInteger_returnsValue() {
		MappingNode node = createNode("count", "42");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Integer> result = reader.getInt("count");
		assertTrue(result.isPresent());
		assertEquals(42, result.get());
	}

	@Test
	void getInt_negativeInteger_returnsValue() {
		MappingNode node = createNode("count", "-42");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Integer> result = reader.getInt("count");
		assertTrue(result.isPresent());
		assertEquals(-42, result.get());
	}

	@Test
	void getInt_nonInteger_returnsEmpty() {
		MappingNode node = createNode("value", "not a number");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Integer> result = reader.getInt("value");
		assertTrue(result.isEmpty());
	}

	// --- getLong tests ---

	@Test
	void getLong_validLong_returnsValue() {
		MappingNode node = createNode("big", "9223372036854775807");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Long> result = reader.getLong("big");
		assertTrue(result.isPresent());
		assertEquals(9223372036854775807L, result.get());
	}

	// --- getFloat tests ---

	@Test
	void getFloat_integerValue_returnsValue() {
		MappingNode node = createNode("value", "42");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Float> result = reader.getFloat("value");
		assertTrue(result.isPresent());
		assertEquals(42.0f, result.get(), 0.001f);
	}

	@Test
	void getFloat_decimalValue_returnsValue() {
		MappingNode node = createNode("value", "3.14");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Float> result = reader.getFloat("value");
		assertTrue(result.isPresent());
		assertEquals(3.14f, result.get(), 0.001f);
	}

	@Test
	void getFloat_leadingDotValue_returnsValue() {
		MappingNode node = createNode("value", ".5");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Float> result = reader.getFloat("value");
		assertTrue(result.isPresent());
		assertEquals(0.5f, result.get(), 0.001f);
	}

	// --- getDouble tests ---

	@Test
	void getDouble_decimalValue_returnsValue() {
		MappingNode node = createNode("value", "3.14159");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Double> result = reader.getDouble("value");
		assertTrue(result.isPresent());
		assertEquals(3.14159, result.get(), 0.00001);
	}

	// --- getBool tests ---

	@Test
	void getBool_trueValue_returnsTrue() {
		MappingNode node = createNode("flag", "true");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Boolean> result = reader.getBool("flag");
		assertTrue(result.isPresent());
		assertEquals(true, result.get());
	}

	@Test
	void getBool_falseValue_returnsFalse() {
		MappingNode node = createNode("flag", "false");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Boolean> result = reader.getBool("flag");
		assertTrue(result.isPresent());
		assertEquals(false, result.get());
	}

	@Test
	void getBool_nonBoolean_returnsEmpty() {
		MappingNode node = createNode("flag", "yes");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Boolean> result = reader.getBool("flag");
		assertTrue(result.isEmpty());
	}

	// --- getByte and getShort tests ---

	@Test
	void getByte_validValue_returnsValue() {
		MappingNode node = createNode("count", "127");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Byte> result = reader.getByte("count");
		assertTrue(result.isPresent());
		assertEquals((byte) 127, result.get());
	}

	@Test
	void getShort_validValue_returnsValue() {
		MappingNode node = createNode("count", "32767");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Short> result = reader.getShort("count");
		assertTrue(result.isPresent());
		assertEquals((short) 32767, result.get());
	}

	// --- has() tests ---
	// has() calls resolve(key) which returns Optional.empty() when key is not found

	@Test
	void has_existingKey_returnsTrue() {
		MappingNode node = createNode("name", "Hello");
		ConfigurationReader reader = new ConfigurationReader(node);

		assertTrue(reader.has("name"));
	}

	@Test
	void has_missingKey_returnsFalse() {
		ConfigurationReader reader = createReader();
		// resolve() returns Optional.empty() when the key is not found
		assertFalse(reader.has("missing"));
	}

	// --- getSection tests ---

	@Test
	void getSection_existingMapping_returnsSection() {
		MappingNode nested = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode nestedKey = new ScalarNode(Tag.STR, "host", null, null, ScalarStyle.PLAIN);
		ScalarNode nestedValue = new ScalarNode(Tag.STR, "localhost", null, null, ScalarStyle.SINGLE_QUOTED);
		nested.getValue().add(new NodeTuple(nestedKey, nestedValue));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, "database", null, null, ScalarStyle.PLAIN);
		root.getValue().add(new NodeTuple(keyNode, nested));

		ConfigurationReader reader = new ConfigurationReader(root);

		Optional<ConfigurationWriter> section = reader.getSection("database");
		assertTrue(section.isPresent());
		assertEquals("localhost", section.get().getString("host").orElse(null));
	}

	@Test
	void getSection_missingKey_returnsEmpty() {
		ConfigurationReader reader = createReader();

		Optional<ConfigurationWriter> section = reader.getSection("missing");
		// resolve returns Optional.empty(), so getSection returns empty
		assertTrue(section.isEmpty());
	}

	// --- getSections tests ---

	@Test
	void getSections_existingSequence_returnsSections() {
		MappingNode item1 = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode key1 = new ScalarNode(Tag.STR, "name", null, null, ScalarStyle.PLAIN);
		ScalarNode val1 = new ScalarNode(Tag.STR, "first", null, null, ScalarStyle.SINGLE_QUOTED);
		item1.getValue().add(new NodeTuple(key1, val1));

		MappingNode item2 = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode key2 = new ScalarNode(Tag.STR, "name", null, null, ScalarStyle.PLAIN);
		ScalarNode val2 = new ScalarNode(Tag.STR, "second", null, null, ScalarStyle.SINGLE_QUOTED);
		item2.getValue().add(new NodeTuple(key2, val2));

		SequenceNode seq = new SequenceNode(Tag.SEQ, List.of(item1, item2), FlowStyle.AUTO);

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, "servers", null, null, ScalarStyle.PLAIN);
		root.getValue().add(new NodeTuple(keyNode, seq));

		ConfigurationReader reader = new ConfigurationReader(root);

		var sections = reader.getSections("servers");
		assertTrue(sections.isPresent());
		assertEquals(2, sections.get().size());
		assertEquals("first", sections.get().get(0).getString("name").orElse(null));
		assertEquals("second", sections.get().get(1).getString("name").orElse(null));
	}

	@Test
	void getSections_missingKey_returnsEmpty() {
		ConfigurationReader reader = createReader();

		var sections = reader.getSections("missing");
		// resolve returns Optional.empty(), so returns empty
		assertTrue(sections.isEmpty());
	}

	// --- getStringList tests ---

	@Test
	void getStringList_existingSequence_returnsList() {
		ScalarNode item1 = new ScalarNode(Tag.STR, "alpha", null, null, ScalarStyle.SINGLE_QUOTED);
		ScalarNode item2 = new ScalarNode(Tag.STR, "beta", null, null, ScalarStyle.SINGLE_QUOTED);
		SequenceNode seq = new SequenceNode(Tag.SEQ, List.of(item1, item2), FlowStyle.AUTO);

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, "tags", null, null, ScalarStyle.PLAIN);
		root.getValue().add(new NodeTuple(keyNode, seq));

		ConfigurationReader reader = new ConfigurationReader(root);

		List<String> result = reader.getStringList("tags");
		assertEquals(2, result.size());
		assertEquals("alpha", result.get(0));
		assertEquals("beta", result.get(1));
	}

	@Test
	void getStringList_missingKey_returnsEmptyList() {
		ConfigurationReader reader = createReader();

		List<String> result = reader.getStringList("missing");
		assertEquals(0, result.size());
	}

	// --- Constructor null check ---

	@Test
	void constructor_nullNode_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> new ConfigurationReader(null));
	}

	// --- yaml() accessor test ---

	@Test
	void yaml_returnsUnderlyingNode() {
		MappingNode node = createNode("name", "Hello");
		ConfigurationReader reader = new ConfigurationReader(node);

		assertSame(node, reader.yaml());
	}

	// --- Type mismatch tests ---

	@Test
	void getLong_nonInteger_returnsEmpty() {
		MappingNode node = createNode("value", "not a number");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Long> result = reader.getLong("value");
		assertTrue(result.isEmpty());
	}

	@Test
	void getFloat_nonNumeric_returnsEmpty() {
		MappingNode node = createNode("value", "not a number");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Float> result = reader.getFloat("value");
		assertTrue(result.isEmpty());
	}

	@Test
	void getDouble_nonNumeric_returnsEmpty() {
		MappingNode node = createNode("value", "not a number");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Double> result = reader.getDouble("value");
		assertTrue(result.isEmpty());
	}

	@Test
	void getByte_nonInteger_returnsEmpty() {
		MappingNode node = createNode("value", "not a number");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Byte> result = reader.getByte("value");
		assertTrue(result.isEmpty());
	}

	@Test
	void getShort_nonInteger_returnsEmpty() {
		MappingNode node = createNode("value", "not a number");
		ConfigurationReader reader = new ConfigurationReader(node);

		Optional<Short> result = reader.getShort("value");
		assertTrue(result.isEmpty());
	}
}
