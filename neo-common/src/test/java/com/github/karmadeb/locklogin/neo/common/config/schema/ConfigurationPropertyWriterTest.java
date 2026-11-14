package com.github.karmadeb.locklogin.neo.common.config.schema;

import com.github.karmadeb.locklogin.neo.api.configuration.schema.SchemaWriter;
import com.github.karmadeb.locklogin.neo.common.config.ConfigurationWriter;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationPropertyWriterTest {

	private ConfigurationWriter createWriter() {
		return new ConfigurationWriter(new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO));
	}

	private ConfigurationSchemaWriter createSchema() {
		return new ConfigurationSchemaWriter(createWriter());
	}

	// --- Property type tests ---

	@Test
	void string_property_writesValue() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("name").string(v -> v.with(() -> "Hello"));

		assertEquals(1, schema.values.size());
		assertTrue(schema.values.containsKey("name"));

		ConfigurationValueWriter<?> value = schema.values.get("name");
		assertEquals("name", value.key());
		assertEquals(ConfigurationValueWriter.Type.STRING, value.type());
	}

	@Test
	void byteNumber_property_writesValue() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("count").byteNumber(v -> v.with(() -> (byte) 42));

		ConfigurationValueWriter<?> value = schema.values.get("count");
		assertEquals(ConfigurationValueWriter.Type.BYTE, value.type());
	}

	@Test
	void shortNumber_property_writesValue() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("count").shortNumber(v -> v.with(() -> (short) 1000));

		ConfigurationValueWriter<?> value = schema.values.get("count");
		assertEquals(ConfigurationValueWriter.Type.SHORT, value.type());
	}

	@Test
	void intNumber_property_writesValue() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("count").intNumber(v -> v.with(() -> 9999));

		ConfigurationValueWriter<?> value = schema.values.get("count");
		assertEquals(ConfigurationValueWriter.Type.INT, value.type());
	}

	@Test
	void longNumber_property_writesValue() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("big").longNumber(v -> v.with(() -> 9223372036854775807L));

		ConfigurationValueWriter<?> value = schema.values.get("big");
		assertEquals(ConfigurationValueWriter.Type.LONG, value.type());
	}

	@Test
	void floatNumber_property_writesValue() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("pi").floatNumber(v -> v.with(() -> 3.14f));

		ConfigurationValueWriter<?> value = schema.values.get("pi");
		assertEquals(ConfigurationValueWriter.Type.FLOAT, value.type());
	}

	@Test
	void doubleNumber_property_writesValue() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("pi").doubleNumber(v -> v.with(() -> 3.14159265358979));

		ConfigurationValueWriter<?> value = schema.values.get("pi");
		assertEquals(ConfigurationValueWriter.Type.DOUBLE, value.type());
	}

	@Test
	void bool_property_writesValue() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("enabled").bool(v -> v.with(() -> true));

		ConfigurationValueWriter<?> value = schema.values.get("enabled");
		assertEquals(ConfigurationValueWriter.Type.BOOLEAN, value.type());
	}

	@Test
	void section_property_writesNestedSection() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("database").section((supplier, _) -> {
			SchemaWriter dbSchema = supplier.get();
			dbSchema.with("host").string(v -> v.with(() -> "localhost"));
		});

		assertEquals(1, schema.values.size());
		assertTrue(schema.values.containsKey("database"));
		assertEquals(ConfigurationValueWriter.Type.MAPPING, schema.values.get("database").type());
	}

	@Test
	void sections_property_writesListOfSections() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("servers").sections((supplier, value) -> {
			List<SchemaWriter> schemas = new ArrayList<>();
			SchemaWriter s1 = supplier.get();
			s1.with("host").string(v -> v.with(() -> "server1"));
			schemas.add(s1);
			value.with(() -> schemas);
		});

		assertEquals(1, schema.values.size());
		assertEquals(ConfigurationValueWriter.Type.MAPPING_LIST, schema.values.get("servers").type());
	}

	@Test
	void list_property_writesStringList() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("tags").list(v -> v.with(() -> List.of("alpha", "beta")));

		assertEquals(1, schema.values.size());
		assertEquals(ConfigurationValueWriter.Type.STRING_LIST, schema.values.get("tags").type());
	}

	// --- Fluent API chaining tests ---

	@Test
	void fluentChaining_multipleProperties() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("name").string(v -> v.with(() -> "default"))
			.with("port").intNumber(v -> v.with(() -> 8080))
			.with("enabled").bool(v -> v.with(() -> true));

		assertEquals(3, schema.values.size());
	}

	@Test
	void fluentChaining_returnsSchemaWriter() {
		ConfigurationSchemaWriter schema = createSchema();

		SchemaWriter result = schema.with("name").string(v -> v.with(() -> "default"));
		assertSame(schema, result);
	}

	// --- Property with() tests ---

	@Test
	void property_withDefault_configured() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("name").string(v -> v.with(() -> "default"));

		ConfigurationValueWriter<?> value = schema.values.get("name");
		assertTrue(value.with().isPresent());
		assertEquals("default", value.with().get().get());
	}

	@Test
	void property_required_value() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("name").string(v -> v.required(() -> "required-default"));

		ConfigurationValueWriter<?> value = schema.values.get("name");
		assertTrue(value.required().isPresent());
		assertEquals("required-default", value.required().get().get());
	}

	@Test
	void property_constant_value() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("version").string(v -> v.constant("1.0.0"));

		ConfigurationValueWriter<?> value = schema.values.get("version");
		assertTrue(value.constant().isPresent());
		assertEquals("1.0.0", value.constant().get());
	}

	@Test
	void property_validator_configured() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("port").intNumber(v -> {
			v.required(() -> 8080);
			v.validator(obj -> obj instanceof Integer && (Integer) obj > 0);
		});

		ConfigurationValueWriter<?> value = schema.values.get("port");
		assertTrue(value.validator().isPresent());
	}

	// --- Property comments tests ---

	@Test
	void property_withComments_preservesComments() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("name", "This is a comment", "Another comment").string(v -> v.with(() -> "default"));

		ConfigurationValueWriter<?> value = schema.values.get("name");
		assertArrayEquals(new String[]{"This is a comment", "Another comment"}, value.comment());
	}

	// --- Schema apply() integration tests ---

	@Test
	void property_string_appliedToNode() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("name").string(v -> v.with(() -> "default"));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<String> value = result.getString("name");
		assertTrue(value.isPresent());
		assertEquals("default", value.get());
	}

	@Test
	void property_int_appliedToNode() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("port").intNumber(v -> v.with(() -> 8080));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<Integer> value = result.getInt("port");
		assertTrue(value.isPresent());
		assertEquals(8080, value.get());
	}

	@Test
	void property_bool_appliedToNode() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("enabled").bool(v -> v.with(() -> true));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		Optional<Boolean> value = result.getBool("enabled");
		assertTrue(value.isPresent());
		assertEquals(true, value.get());
	}

	@Test
	void property_list_appliedToNode() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("tags").list(v -> v.with(() -> List.of("alpha", "beta")));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		schema.apply(root);

		ConfigurationWriter result = new ConfigurationWriter(root);
		List<String> tags = result.getStringList("tags");
		assertEquals(2, tags.size());
		assertEquals("alpha", tags.get(0));
		assertEquals("beta", tags.get(1));
	}

	// --- Property key and comment accessors ---

	@Test
	void property_key_accessor() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("myKey").string(v -> v.with(() -> "value"));

		ConfigurationValueWriter<?> value = schema.values.get("myKey");
		assertEquals("myKey", value.key());
	}

	@Test
	void property_comment_accessor() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("myKey", "comment1", "comment2").string(v -> v.with(() -> "value"));

		ConfigurationValueWriter<?> value = schema.values.get("myKey");
		assertArrayEquals(new String[]{"comment1", "comment2"}, value.comment());
	}

	@Test
	void property_noComments_returnsEmptyArray() {
		ConfigurationSchemaWriter schema = createSchema();

		schema.with("myKey").string(v -> v.with(() -> "value"));

		ConfigurationValueWriter<?> value = schema.values.get("myKey");
		assertEquals(0, value.comment().length);
	}
}
