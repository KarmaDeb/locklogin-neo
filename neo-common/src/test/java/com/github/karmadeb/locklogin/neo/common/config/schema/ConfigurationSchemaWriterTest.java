package com.github.karmadeb.locklogin.neo.common.config.schema;

import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.schema.SchemaWriter;
import com.github.karmadeb.locklogin.neo.common.config.ConfigurationWriter;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationSchemaWriterTest {

	private ConfigurationWriter createWriter() {
		return new ConfigurationWriter(new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO));
	}

	// --- Schema with() tests ---

	@Test
	void schema_withDefault_writesDefaultValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("name").string(v -> v.with(() -> "default"));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<String> value = result.getString("name");
		assertTrue(value.isPresent());
		assertEquals("default", value.get());
	}

	@Test
	void schema_withDefault_int_writesDefaultValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("port").intNumber(v -> v.with(() -> 8080));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<Integer> value = result.getInt("port");
		assertTrue(value.isPresent());
		assertEquals(8080, value.get());
	}

	@Test
	void schema_withDefault_bool_writesDefaultValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("enabled").bool(v -> v.with(() -> true));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<Boolean> value = result.getBool("enabled");
		assertTrue(value.isPresent());
		assertEquals(true, value.get());
	}

	// --- Schema required() tests ---

	@Test
	void schema_required_writesDefaultValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		// required() alone doesn't write defaults for missing keys — need with() + required()
		schema.with("name").string(v -> {
			v.with(() -> "default");
			v.required(() -> "required-default");
		});

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<String> value = result.getString("name");
		assertTrue(value.isPresent());
		assertEquals("required-default", value.get());
	}

	@Test
	void schema_required_int_writesDefaultValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		// required() alone doesn't write defaults for missing keys — need with() + required()
		schema.with("port").intNumber(v -> {
			v.with(() -> 8080);
			v.required(() -> 443);
		});

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<Integer> value = result.getInt("port");
		assertTrue(value.isPresent());
		assertEquals(443, value.get());
	}

	// --- Schema constant() tests ---

	@Test
	void schema_constant_writesConstantValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("version").string(v -> v.constant("1.0.0"));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<String> value = result.getString("version");
		assertTrue(value.isPresent());
		assertEquals("1.0.0", value.get());
	}

	@Test
	void schema_constant_int_writesConstantValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("maxRetries").intNumber(v -> v.constant(3));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<Integer> value = result.getInt("maxRetries");
		assertTrue(value.isPresent());
		assertEquals(3, value.get());
	}

	// --- Schema validator tests ---

	@Test
	void schema_validator_validValue_keepsValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("port").intNumber(v -> {
			v.required(() -> 8080);
			v.validator(obj -> obj instanceof Integer && (Integer) obj > 0);
		});

		// Pre-fill root with a valid value so apply() finds it and validates
		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, "port", null, null, ScalarStyle.PLAIN);
		ScalarNode valNode = new ScalarNode(Tag.INT, "9090", null, null, ScalarStyle.PLAIN);
		root.getValue().add(new NodeTuple(keyNode, valNode));

		schema.apply(root);

		// The valid value should be preserved in the root node, not replaced
		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<Integer> value = result.getInt("port");
		assertTrue(value.isPresent());
		assertEquals(9090, value.get());
	}

	@Test
	void schema_validator_replacesInvalidValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("port").intNumber(v -> {
			v.with(() -> 8080);
			v.required(() -> 8080);
			v.validator(obj -> obj instanceof Integer && (Integer) obj > 0);
		});

		// Pre-fill root with an invalid value so apply() finds it and validates
		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, "port", null, null, ScalarStyle.PLAIN);
		ScalarNode valNode = new ScalarNode(Tag.INT, "-1", null, null, ScalarStyle.PLAIN);
		root.getValue().add(new NodeTuple(keyNode, valNode));

		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<Integer> value = result.getInt("port");
		assertTrue(value.isPresent());
		// Invalid value should be replaced with the required default
		assertEquals(8080, value.get());
	}

	// --- Schema section tests ---

	@Test
	void schema_section_createsSection() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("database").section((supplier, value) -> {
			SchemaWriter dbSchema = supplier.get();
			dbSchema.with("host").string(v -> v.with(() -> "localhost"));
			dbSchema.with("port").intNumber(v -> v.with(() -> 5432));
			// The section default is provided via the section callback itself
			value.with(() -> dbSchema);
		});

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<ConfigurationWriter> section = result.getSection("database");
		// The section is created as a MappingNode by writeSectionDefault
		assertTrue(section.isPresent());
		// Nested values should be populated
		assertEquals("localhost", section.get().getString("host").orElse(null));
		assertEquals(5432, section.get().getInt("port").orElse(-1));
	}

	// --- Schema sections (list of sections) tests ---

	@Test
	void schema_sections_createsListOfSections() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("servers").sections((supplier, value) -> {
			List<SchemaWriter> schemas = new ArrayList<>();
			SchemaWriter s1 = supplier.get();
			s1.with("host").string(v -> v.with(() -> "server1"));
			schemas.add(s1);
			value.with(() -> schemas);
		});

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		var sections = result.getSections("servers");
		assertTrue(sections.isPresent());
		assertEquals(1, sections.get().size());
	}

	// --- Schema list tests ---

	@Test
	void schema_list_writesStringList() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("tags").list(v -> v.with(() -> List.of("alpha", "beta")));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		List<String> tags = result.getStringList("tags");
		assertEquals(2, tags.size());
		assertEquals("alpha", tags.get(0));
		assertEquals("beta", tags.get(1));
	}

	// --- Schema comments tests ---

	@Test
	void schema_withComments_setsComments() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("name", "This is a config comment").string(v -> v.with(() -> "default"));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		// Verify the value was written to the schema's writer
		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<String> value = result.getString("name");
		assertTrue(value.isPresent());
		assertEquals("default", value.get());
	}

	// --- Schema multiple properties tests ---

	@Test
	void schema_multipleProperties() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("name").string(v -> v.with(() -> "default-name"));
		schema.with("port").intNumber(v -> v.with(() -> 8080));
		schema.with("enabled").bool(v -> v.with(() -> true));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		assertEquals("default-name", result.getString("name").orElse(null));
		assertEquals(8080, result.getInt("port").orElse(-1));
		assertEquals(true, result.getBool("enabled").orElse(false));
	}

	// --- Schema apply() on existing nodes ---

	@Test
	void schema_apply_fillsMissingKeys() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("name").string(v -> v.with(() -> "default"));
		schema.with("port").intNumber(v -> v.with(() -> 8080));

		// Pre-fill root with only one key
		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, "name", null, null, ScalarStyle.PLAIN);
		ScalarNode valNode = new ScalarNode(Tag.STR, "existing", null, null, ScalarStyle.SINGLE_QUOTED);
		root.getValue().add(new NodeTuple(keyNode, valNode));

		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		// Existing key should be preserved
		assertEquals("existing", result.getString("name").orElse(null));
		// Missing key should be filled with default
		assertEquals(8080, result.getInt("port").orElse(-1));
	}

	// --- Schema build() test ---

	@Test
	void schema_build_returnsWriter() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		ConfigWriter result = schema.build();
		assertSame(writer, result);
	}

	// --- All numeric type tests ---

	@Test
	void schema_byteNumber_writesDefaultValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("count").byteNumber(v -> v.with(() -> (byte) 42));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<Byte> value = result.getByte("count");
		assertTrue(value.isPresent());
		assertEquals((byte) 42, value.get());
	}

	@Test
	void schema_shortNumber_writesDefaultValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("count").shortNumber(v -> v.with(() -> (short) 1000));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<Short> value = result.getShort("count");
		assertTrue(value.isPresent());
		assertEquals((short) 1000, value.get());
	}

	@Test
	void schema_longNumber_writesDefaultValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("big").longNumber(v -> v.with(() -> 9223372036854775807L));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<Long> value = result.getLong("big");
		assertTrue(value.isPresent());
		assertEquals(9223372036854775807L, value.get());
	}

	@Test
	void schema_floatNumber_writesDefaultValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("pi").floatNumber(v -> v.with(() -> 3.14f));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<Float> value = result.getFloat("pi");
		assertTrue(value.isPresent());
		assertEquals(3.14f, value.get(), 0.001f);
	}

	@Test
	void schema_doubleNumber_writesDefaultValue() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("pi").doubleNumber(v -> v.with(() -> 3.14159265358979));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<Double> value = result.getDouble("pi");
		assertTrue(value.isPresent());
		assertEquals(3.14159265358979, value.get(), 0.0000001);
	}

	// --- Constructor null check ---

	@Test
	void constructor_nullWriter_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> new ConfigurationSchemaWriter(null));
	}

	// --- Schema values map test ---

	@Test
	void schema_valuesStoredInMap() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("name").string(v -> v.with(() -> "default"));
		schema.with("port").intNumber(v -> v.with(() -> 8080));

		// Values should be stored in the schema's values map
		assertEquals(2, schema.values.size());
		assertTrue(schema.values.containsKey("name"));
		assertTrue(schema.values.containsKey("port"));
	}

	// --- No default value test ---

	@Test
	void schema_noDefault_doesNotWrite() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("name").string(_ -> {
			// No default, required, or constant set
		});

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		// No value should be written since no default was provided
		ConfigurationWriter reader = new ConfigurationWriter(root);
		assertEquals(0, reader.yaml().getValue().size());
	}

	// --- Schema merge() tests ---

	@Test
	void schema_merge_replacesExistingKey() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("name").string(v -> v.with(() -> "original"));
		schema.with("port").intNumber(v -> v.with(() -> 8080));

		// Create another schema with overlapping keys
		ConfigurationWriter otherWriter = createWriter();
		ConfigurationSchemaWriter other = new ConfigurationSchemaWriter(otherWriter);
		other.with("name").string(v -> v.with(() -> "merged"));
		other.with("host").string(v -> v.with(() -> "localhost"));

		SchemaWriter result = schema.merge(other);

		// Should return a different schema instance
		assertNotSame(schema, result);
		// Original schema should be unchanged
		assertEquals(2, schema.values.size());
		// The merged result should have all keys
		assertEquals(3, ((ConfigurationSchemaWriter) result).values.size());
		assertTrue(((ConfigurationSchemaWriter) result).values.containsKey("name"));
		// Since neither schema uses constant(), the merged value should not be constant
		assertNull(((ConfigurationSchemaWriter) result).values.get("name").constant().orElse(null));
		// The other's with() value should replace this's with() value
		assertEquals("merged", ((ConfigurationSchemaWriter) result).values.get("name").with().orElseThrow().get());
		assertTrue(((ConfigurationSchemaWriter) result).values.containsKey("port"));
		assertTrue(((ConfigurationSchemaWriter) result).values.containsKey("host"));
	}

	@Test
	void schema_merge_addsNewKeys() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("name").string(v -> v.with(() -> "default"));

		ConfigurationWriter otherWriter = createWriter();
		ConfigurationSchemaWriter other = new ConfigurationSchemaWriter(otherWriter);
		other.with("port").intNumber(v -> v.with(() -> 9090));
		other.with("enabled").bool(v -> v.with(() -> true));

		SchemaWriter result = schema.merge(other);

		// Merged result should have all keys
		assertEquals(3, ((ConfigurationSchemaWriter) result).values.size());
		assertTrue(((ConfigurationSchemaWriter) result).values.containsKey("name"));
		assertTrue(((ConfigurationSchemaWriter) result).values.containsKey("port"));
		assertTrue(((ConfigurationSchemaWriter) result).values.containsKey("enabled"));
	}

	@Test
	void schema_merge_appliesMergedValues() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("name").string(v -> v.with(() -> "original"));

		ConfigurationWriter otherWriter = createWriter();
		ConfigurationSchemaWriter other = new ConfigurationSchemaWriter(otherWriter);
		other.with("name").string(v -> v.with(() -> "overridden"));

		SchemaWriter merged = schema.merge(other);

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		((ConfigurationSchemaWriter) merged).apply(root);

		ConfigurationWriter configResult = new ConfigurationWriter(root);
		Optional<String> value = configResult.getString("name");
		assertTrue(value.isPresent());
		assertEquals("overridden", value.get());
	}

	@Test
	void schema_merge_null_throwsNullPointerException() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> schema.merge(null));
	}

	@Test
	void schema_merge_preservesNonOverlappingKeys() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("name").string(v -> v.with(() -> "default"));
		schema.with("port").intNumber(v -> v.with(() -> 8080));

		ConfigurationWriter otherWriter = createWriter();
		ConfigurationSchemaWriter other = new ConfigurationSchemaWriter(otherWriter);
		other.with("host").string(v -> v.with(() -> "localhost"));

		SchemaWriter result = schema.merge(other);

		// All three keys should be present in the merged result
		assertEquals(3, ((ConfigurationSchemaWriter) result).values.size());
		assertEquals("default", ((ConfigurationSchemaWriter) result).values.get("name").with().orElseThrow().get());
		assertEquals(8080, ((ConfigurationSchemaWriter) result).values.get("port").with().orElseThrow().get());
		assertEquals("localhost", ((ConfigurationSchemaWriter) result).values.get("host").with().orElseThrow().get());
	}

	@Test
	void schema_merge_constantReplacesNonConstant() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("name").string(v -> v.with(() -> "original"));

		ConfigurationWriter otherWriter = createWriter();
		ConfigurationSchemaWriter other = new ConfigurationSchemaWriter(otherWriter);
		other.with("name").string(v -> v.constant("constant-value"));

		SchemaWriter result = schema.merge(other);

		// The constant value should completely replace the non-constant value
		assertEquals("constant-value", ((ConfigurationSchemaWriter) result).values.get("name").constant().orElse(null));
		assertEquals("constant-value", ((ConfigurationSchemaWriter) result).values.get("name").with().orElseThrow().get());
	}

	@Test
	void schema_merge_stringListWithStringList() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("tags").list(v -> v.with(() -> List.of("alpha", "beta")));

		ConfigurationWriter otherWriter = createWriter();
		ConfigurationSchemaWriter other = new ConfigurationSchemaWriter(otherWriter);
		other.with("tags").list(v -> v.with(() -> List.of("gamma", "delta")));

		SchemaWriter result = schema.merge(other);

		// noinspection unchecked
		List<String> mergedList = (List<String>) ((ConfigurationSchemaWriter) result).values.get("tags").with().orElseThrow().get();
		assertEquals(4, mergedList.size());
		assertEquals("alpha", mergedList.get(0));
		assertEquals("beta", mergedList.get(1));
		assertEquals("gamma", mergedList.get(2));
		assertEquals("delta", mergedList.get(3));
	}

	@Test
	void schema_merge_stringWithStringList() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("tags").string(v -> v.with(() -> "single"));

		ConfigurationWriter otherWriter = createWriter();
		ConfigurationSchemaWriter other = new ConfigurationSchemaWriter(otherWriter);
		other.with("tags").list(v -> v.with(() -> List.of("alpha", "beta")));

		SchemaWriter result = schema.merge(other);

		// noinspection unchecked
		List<String> mergedList = (List<String>) ((ConfigurationSchemaWriter) result).values.get("tags").with().orElseThrow().get();
		assertEquals(3, mergedList.size());
		assertEquals("alpha", mergedList.get(0));
		assertEquals("beta", mergedList.get(1));
		assertEquals("single", mergedList.get(2));
	}

	@Test
	void schema_merge_mappingListWithMappingList() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("servers").sections((supplier, value) -> {
			List<SchemaWriter> schemas = new ArrayList<>();
			SchemaWriter s1 = supplier.get();
			s1.with("host").string(v -> v.with(() -> "server1"));
			schemas.add(s1);
			value.with(() -> schemas);
		});

		ConfigurationWriter otherWriter = createWriter();
		ConfigurationSchemaWriter other = new ConfigurationSchemaWriter(otherWriter);
		other.with("servers").sections((supplier, value) -> {
			List<SchemaWriter> schemas = new ArrayList<>();
			SchemaWriter s2 = supplier.get();
			s2.with("host").string(v -> v.with(() -> "server2"));
			schemas.add(s2);
			value.with(() -> schemas);
		});

		SchemaWriter result = schema.merge(other);

		// noinspection unchecked
		List<SchemaWriter> mergedList = (List<SchemaWriter>) ((ConfigurationSchemaWriter) result).values.get("servers").with().orElseThrow().get();
		assertEquals(2, mergedList.size());
	}

	@Test
	void schema_merge_mappingWithMapping() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("database").section((supplier, value) -> {
			SchemaWriter dbSchema = supplier.get();
			dbSchema.with("host").string(v -> v.with(() -> "localhost"));
			value.with(() -> dbSchema);
		});

		ConfigurationWriter otherWriter = createWriter();
		ConfigurationSchemaWriter other = new ConfigurationSchemaWriter(otherWriter);
		other.with("database").section((supplier, value) -> {
			SchemaWriter dbSchema = supplier.get();
			dbSchema.with("port").intNumber(v -> v.with(() -> 5432));
			value.with(() -> dbSchema);
		});

		SchemaWriter result = schema.merge(other);

		// The merged schema should have both host and port
		SchemaWriter mergedSchema = (SchemaWriter) ((ConfigurationSchemaWriter) result).values.get("database").with().orElseThrow().get();
		assertInstanceOf(ConfigurationSchemaWriter.class, mergedSchema);
		ConfigurationSchemaWriter mergedConfig = (ConfigurationSchemaWriter) mergedSchema;
		assertTrue(mergedConfig.values.containsKey("host"));
		assertTrue(mergedConfig.values.containsKey("port"));
		assertEquals("localhost", mergedConfig.values.get("host").with().orElseThrow().get());
		assertEquals(5432, mergedConfig.values.get("port").with().orElseThrow().get());
	}

	@Test
	void schema_merge_mappingWithMappingList() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("servers").section((supplier, value) -> {
			SchemaWriter dbSchema = supplier.get();
			dbSchema.with("host").string(v -> v.with(() -> "single"));
			value.with(() -> dbSchema);
		});

		ConfigurationWriter otherWriter = createWriter();
		ConfigurationSchemaWriter other = new ConfigurationSchemaWriter(otherWriter);
		other.with("servers").sections((supplier, value) -> {
			List<SchemaWriter> schemas = new ArrayList<>();
			SchemaWriter s1 = supplier.get();
			s1.with("host").string(v -> v.with(() -> "server1"));
			schemas.add(s1);
			value.with(() -> schemas);
		});

		SchemaWriter result = schema.merge(other);

		// noinspection unchecked
		List<SchemaWriter> mergedList = (List<SchemaWriter>) ((ConfigurationSchemaWriter) result).values.get("servers").with().orElseThrow().get();
		assertEquals(2, mergedList.size());
	}

	@Test
	void schema_merge_mappingListWithMapping() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("servers").sections((supplier, value) -> {
			List<SchemaWriter> schemas = new ArrayList<>();
			SchemaWriter s1 = supplier.get();
			s1.with("host").string(v -> v.with(() -> "server1"));
			schemas.add(s1);
			value.with(() -> schemas);
		});

		ConfigurationWriter otherWriter = createWriter();
		ConfigurationSchemaWriter other = new ConfigurationSchemaWriter(otherWriter);
		other.with("servers").section((supplier, value) -> {
			SchemaWriter dbSchema = supplier.get();
			dbSchema.with("host").string(v -> v.with(() -> "single"));
			value.with(() -> dbSchema);
		});

		SchemaWriter result = schema.merge(other);

		// noinspection unchecked
		List<SchemaWriter> mergedList = (List<SchemaWriter>) ((ConfigurationSchemaWriter) result).values.get("servers").with().orElseThrow().get();
		assertEquals(2, mergedList.size());
	}

	@Test
	void schema_merge_validatorsCombined() {
		ConfigurationWriter writer = createWriter();
		ConfigurationSchemaWriter schema = new ConfigurationSchemaWriter(writer);

		schema.with("port").intNumber(v -> {
			v.with(() -> 8080);
			v.validator(obj -> obj instanceof Integer && (Integer) obj > 0);
		});

		ConfigurationWriter otherWriter = createWriter();
		ConfigurationSchemaWriter other = new ConfigurationSchemaWriter(otherWriter);
		other.with("port").intNumber(v -> {
			v.with(() -> 9090);
			v.validator(obj -> obj instanceof Integer && (Integer) obj < 10000);
		});

		SchemaWriter result = schema.merge(other);

		// The merged value should have a combined validator
		assertTrue(((ConfigurationSchemaWriter) result).values.get("port").validator().isPresent());
	}
}
