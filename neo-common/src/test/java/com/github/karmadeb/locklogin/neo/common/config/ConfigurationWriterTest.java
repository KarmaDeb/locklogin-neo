package com.github.karmadeb.locklogin.neo.common.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationWriterTest {

	private ConfigurationWriter createWriter() {
		return new ConfigurationWriter(new MappingNode(Tag.MAP, new java.util.ArrayList<>(), FlowStyle.AUTO));
	}

	// --- Scalar write & read tests ---

	@Test
	void writeString_writesAndReadsValue() {
		ConfigurationWriter writer = createWriter();
		writer.writeString("name", "Hello");

		Optional<String> result = writer.getString("name");
		assertTrue(result.isPresent());
		assertEquals("Hello", result.get());
	}

	@Test
	void writeByte_writesAndReadsValue() {
		ConfigurationWriter writer = createWriter();
		writer.writeByte("count", (byte) 42);

		Optional<Byte> result = writer.getByte("count");
		assertTrue(result.isPresent());
		assertEquals((byte) 42, result.get());
	}

	@Test
	void writeShort_writesAndReadsValue() {
		ConfigurationWriter writer = createWriter();
		writer.writeShort("count", (short) 1000);

		Optional<Short> result = writer.getShort("count");
		assertTrue(result.isPresent());
		assertEquals((short) 1000, result.get());
	}

	@Test
	void writeInt_writesAndReadsValue() {
		ConfigurationWriter writer = createWriter();
		writer.writeInt("count", 9999);

		Optional<Integer> result = writer.getInt("count");
		assertTrue(result.isPresent());
		assertEquals(9999, result.get());
	}

	@Test
	void writeLong_writesAndReadsValue() {
		ConfigurationWriter writer = createWriter();
		writer.writeLong("big", 9223372036854775807L);

		Optional<Long> result = writer.getLong("big");
		assertTrue(result.isPresent());
		assertEquals(9223372036854775807L, result.get());
	}

	@Test
	void writeFloat_writesAndReadsValue() {
		ConfigurationWriter writer = createWriter();
		writer.writeFloat("pi", 3.14f);

		Optional<Float> result = writer.getFloat("pi");
		assertTrue(result.isPresent());
		assertEquals(3.14f, result.get(), 0.001f);
	}

	@Test
	void writeDouble_writesAndReadsValue() {
		ConfigurationWriter writer = createWriter();
		writer.writeDouble("pi", 3.14159265358979);

		Optional<Double> result = writer.getDouble("pi");
		assertTrue(result.isPresent());
		assertEquals(3.14159265358979, result.get(), 0.0000001);
	}

	@Test
	void writeBool_writesAndReadsValue() {
		ConfigurationWriter writer = createWriter();
		writer.writeBool("enabled", true);

		Optional<Boolean> result = writer.getBool("enabled");
		assertTrue(result.isPresent());
		assertEquals(true, result.get());
	}

	@Test
	void writeBool_writesFalse() {
		ConfigurationWriter writer = createWriter();
		writer.writeBool("disabled", false);

		Optional<Boolean> result = writer.getBool("disabled");
		assertTrue(result.isPresent());
		assertEquals(false, result.get());
	}

	// --- Overwrite tests (adds duplicate tuple, resolve returns first match) ---

	@Test
	void writeString_addsDuplicateTuple() {
		ConfigurationWriter writer = createWriter();
		writer.writeString("name", "First");
		writer.writeString("name", "Second");

		// resolve() finds the first matching tuple, so getString returns the first value
		Optional<String> result = writer.getString("name");
		assertTrue(result.isPresent());
		assertEquals("First", result.get());

		// Both tuples exist in the mapping node
		assertEquals(2, writer.yaml().getValue().size());
	}

	@Test
	void writeInt_addsDuplicateTuple() {
		ConfigurationWriter writer = createWriter();
		writer.writeInt("count", 1);
		writer.writeInt("count", 2);

		Optional<Integer> result = writer.getInt("count");
		assertTrue(result.isPresent());
		assertEquals(1, result.get());
	}

	// --- has() tests ---
	// has() calls resolve(key) which returns Optional.empty() when key is not found

	@Test
	void has_returnsTrueForExistingKey() {
		ConfigurationWriter writer = createWriter();
		writer.writeString("name", "Hello");

		assertTrue(writer.has("name"));
	}

	@Test
	void has_returnsFalseForMissingKey() {
		ConfigurationWriter writer = createWriter();
		// resolve() returns Optional.empty() when the key is not found
		assertFalse(writer.has("missing"));
	}

	// --- Missing key read tests ---
	// When a key is not found, resolve returns Optional.empty()

	@Test
	void getString_missingKey_returnsEmpty() {
		ConfigurationWriter writer = createWriter();

		Optional<String> result = writer.getString("missing");
		// resolve returns Optional.empty(), so getString returns empty
		assertTrue(result.isEmpty());
	}

	@Test
	void getInt_missingKey_returnsEmpty() {
		ConfigurationWriter writer = createWriter();

		Optional<Integer> result = writer.getInt("missing");
		assertTrue(result.isEmpty());
	}

	@Test
	void getBool_missingKey_returnsEmpty() {
		ConfigurationWriter writer = createWriter();

		Optional<Boolean> result = writer.getBool("missing");
		assertTrue(result.isEmpty());
	}

	// --- Type mismatch tests ---

	@Test
	void getInt_stringValue_returnsEmpty() {
		ConfigurationWriter writer = createWriter();
		writer.writeString("value", "not a number");

		Optional<Integer> result = writer.getInt("value");
		assertTrue(result.isEmpty());
	}

	@Test
	void getBool_nonBooleanString_returnsEmpty() {
		ConfigurationWriter writer = createWriter();
		writer.writeString("value", "yes");

		Optional<Boolean> result = writer.getBool("value");
		assertTrue(result.isEmpty());
	}

	// --- Section tests ---

	@Test
	void writeSection_createsNestedSection() {
		ConfigurationWriter writer = createWriter();
		writer.writeSection("database", section -> {
			section.writeString("host", "localhost");
			section.writeInt("port", 5432);
		});

		Optional<ConfigurationWriter> section = writer.getSection("database");
		assertTrue(section.isPresent());
		assertEquals("localhost", section.get().getString("host").orElse(null));
		assertEquals(5432, section.get().getInt("port").orElse(-1));
	}

	@Test
	void writeSection_nestedSection() {
		ConfigurationWriter writer = createWriter();
		writer.writeSection("app",
				app -> app.writeSection("database",
				db -> db.writeString("url", "jdbc:postgresql://localhost/db")));

		Optional<ConfigurationWriter> app = writer.getSection("app");
		assertTrue(app.isPresent());

		Optional<ConfigurationWriter> db = app.get().getSection("database");
		assertTrue(db.isPresent());
		assertEquals("jdbc:postgresql://localhost/db", db.get().getString("url").orElse(null));
	}

	// --- Sections (list of sections) tests ---

	@Test
	void writeSections_createsListOfSections() {
		ConfigurationWriter writer = createWriter();
		writer.writeSections("servers", supplier -> {
			List<com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter> writers = new java.util.ArrayList<>();

			com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter s1 = supplier.get();
			s1.writeString("host", "server1");
			s1.writeInt("port", 8080);
			writers.add(s1);

			com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter s2 = supplier.get();
			s2.writeString("host", "server2");
			s2.writeInt("port", 8081);
			writers.add(s2);

			return writers;
		});

		var sections = writer.getSections("servers");
		assertTrue(sections.isPresent());
		assertEquals(2, sections.get().size());

		assertEquals("server1", sections.get().get(0).getString("host").orElse(null));
		assertEquals(8080, sections.get().get(0).getInt("port").orElse(-1));
		assertEquals("server2", sections.get().get(1).getString("host").orElse(null));
		assertEquals(8081, sections.get().get(1).getInt("port").orElse(-1));
	}

	// --- String list tests ---

	@Test
	void writeStringList_writesAndReadsList() {
		ConfigurationWriter writer = createWriter();
		writer.writeStringList("tags", List.of("alpha", "beta", "gamma"));

		List<String> result = writer.getStringList("tags");
		assertEquals(3, result.size());
		assertEquals("alpha", result.get(0));
		assertEquals("beta", result.get(1));
		assertEquals("gamma", result.get(2));
	}

	@Test
	void writeStringList_emptyList() {
		ConfigurationWriter writer = createWriter();
		writer.writeStringList("tags", List.of());

		List<String> result = writer.getStringList("tags");
		assertEquals(0, result.size());
	}

	@Test
	void getStringList_missingKey_returnsEmptyList() {
		ConfigurationWriter writer = createWriter();

		List<String> result = writer.getStringList("missing");
		// resolve returns Optional.empty(), so returns empty list
		assertEquals(0, result.size());
	}

	// --- Comment tests ---

	@Test
	void comment_onExistingKey_setsComment() {
		ConfigurationWriter writer = createWriter();
		writer.writeString("name", "Hello");
		writer.comment("name", "This is a comment");

		// The comment is stored on the key node; verify the key still exists
		assertTrue(writer.has("name"));

		// Verify the comment is on the key node, not the value node
		List<NodeTuple> tuples = writer.yaml().getValue();
		assertEquals(1, tuples.size());
		ScalarNode keyNode = (ScalarNode) tuples.getFirst().getKeyNode();
		assertNotNull(keyNode.getBlockComments());
		assertEquals(1, keyNode.getBlockComments().size());
		assertEquals(" This is a comment", keyNode.getBlockComments().getFirst().getValue());

		// Value node should not have the comment
		ScalarNode valueNode = (ScalarNode) tuples.getFirst().getValueNode();
		assertTrue(valueNode.getBlockComments() == null || valueNode.getBlockComments().isEmpty());
	}

	@Test
	void comment_onMissingKey_doesNothing() {
		ConfigurationWriter writer = createWriter();
		// Should not throw — resolve returns Optional.empty(), so comment is not set
		writer.comment("missing", "This should not appear");

		// No new key tuple was created by comment()
		assertEquals(0, writer.yaml().getValue().size());
	}

	// --- Schema accessor test ---

	@Test
	void schema_returnsSchemaWriter() {
		ConfigurationWriter writer = createWriter();

		assertNotNull(writer.schema());
	}

	// --- Negative number tests ---

	@Test
	void writeInt_negativeValue() {
		ConfigurationWriter writer = createWriter();
		writer.writeInt("neg", -42);

		Optional<Integer> result = writer.getInt("neg");
		assertTrue(result.isPresent());
		assertEquals(-42, result.get());
	}

	@Test
	void writeLong_negativeValue() {
		ConfigurationWriter writer = createWriter();
		writer.writeLong("neg", -100L);

		Optional<Long> result = writer.getLong("neg");
		assertTrue(result.isPresent());
		assertEquals(-100L, result.get());
	}

	// --- Float from int pattern test ---

	@Test
	void getFloat_intValue_parsesAsFloat() {
		ConfigurationWriter writer = createWriter();
		writer.writeInt("value", 42);

		Optional<Float> result = writer.getFloat("value");
		assertTrue(result.isPresent());
		assertEquals(42.0f, result.get(), 0.001f);
	}

	@Test
	void getDouble_intValue_parsesAsDouble() {
		ConfigurationWriter writer = createWriter();
		writer.writeInt("value", 42);

		Optional<Double> result = writer.getDouble("value");
		assertTrue(result.isPresent());
		assertEquals(42.0, result.get(), 0.001);
	}

	// --- YAML node structure tests ---

	@Test
	void writeString_createsNodeTuple() {
		ConfigurationWriter writer = createWriter();
		writer.writeString("name", "Hello");

		assertEquals(1, writer.yaml().getValue().size());
		NodeTuple tuple = writer.yaml().getValue().getFirst();
		assertInstanceOf(ScalarNode.class, tuple.getKeyNode());
		assertEquals("name", ((ScalarNode) tuple.getKeyNode()).getValue());
	}

	@Test
	void writeMultipleKeys_createsMultipleTuples() {
		ConfigurationWriter writer = createWriter();
		writer.writeString("name", "Hello");
		writer.writeInt("count", 42);

		assertEquals(2, writer.yaml().getValue().size());
	}

	@Test
	void yaml_returnsUnderlyingNode() {
		ConfigurationWriter writer = createWriter();
		MappingNode node = writer.yaml();
		assertNotNull(node);
		assertEquals(Tag.MAP, node.getTag());
	}
}
