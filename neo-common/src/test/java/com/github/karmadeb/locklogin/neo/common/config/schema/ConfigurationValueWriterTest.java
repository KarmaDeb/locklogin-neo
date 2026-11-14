package com.github.karmadeb.locklogin.neo.common.config.schema;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationValueWriterTest {

	// --- Type enum tests ---

	@Test
	void type_values_count() {
		// STRING, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BOOLEAN, MAPPING, STRING_LIST, MAPPING_LIST
		assertEquals(11, ConfigurationValueWriter.Type.values().length);
	}

	@Test
	void type_STRING_parsesSuccessfully() {
		Object result = ConfigurationValueWriter.Type.STRING.apply("hello");
		assertEquals("hello", result);
	}

	@Test
	void type_INT_parsesSuccessfully() {
		Object result = ConfigurationValueWriter.Type.INT.apply("42");
		assertEquals(42, result);
	}

	@Test
	void type_LONG_parsesSuccessfully() {
		Object result = ConfigurationValueWriter.Type.LONG.apply("9223372036854775807");
		assertEquals(9223372036854775807L, result);
	}

	@Test
	void type_FLOAT_parsesSuccessfully() {
		Object result = ConfigurationValueWriter.Type.FLOAT.apply("3.14");
		assertEquals(3.14f, result);
	}

	@Test
	void type_DOUBLE_parsesSuccessfully() {
		Object result = ConfigurationValueWriter.Type.DOUBLE.apply("3.14159");
		assertEquals(3.14159, result);
	}

	@Test
	void type_BYTE_parsesSuccessfully() {
		Object result = ConfigurationValueWriter.Type.BYTE.apply("42");
		assertEquals((byte) 42, result);
	}

	@Test
	void type_SHORT_parsesSuccessfully() {
		Object result = ConfigurationValueWriter.Type.SHORT.apply("1000");
		assertEquals((short) 1000, result);
	}

	@Test
	void type_BOOLEAN_parsesSuccessfully() {
		Object result = ConfigurationValueWriter.Type.BOOLEAN.apply("true");
		assertEquals(true, result);
	}

	@Test
	void type_MAPPING_throwsRuntimeException() {
		// MAPPING type has no parser, throws UnsupportedOperationException wrapped in RuntimeException
		assertThrows(RuntimeException.class, () -> ConfigurationValueWriter.Type.MAPPING.apply("test"));
	}

	@Test
	void type_STRING_LIST_throwsRuntimeException() {
		assertThrows(RuntimeException.class, () -> ConfigurationValueWriter.Type.STRING_LIST.apply("test"));
	}

	@Test
	void type_MAPPING_LIST_throwsRuntimeException() {
		assertThrows(RuntimeException.class, () -> ConfigurationValueWriter.Type.MAPPING_LIST.apply("test"));
	}

	@Test
	void type_INT_invalidValue_throwsRuntimeException() {
		// Invalid int value causes NumberFormatException, wrapped in RuntimeException
		assertThrows(RuntimeException.class, () -> ConfigurationValueWriter.Type.INT.apply("not a number"));
	}

	// --- Constructor accessor tests ---

	@Test
	void constructor_setsKey() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);
		assertEquals("name", value.key());
	}

	@Test
	void constructor_setsType() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);
		assertEquals(ConfigurationValueWriter.Type.STRING, value.type());
	}

	@Test
	void constructor_setsComment() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{"comment1", "comment2"},
				ConfigurationValueWriter.Type.STRING);
		assertArrayEquals(new String[]{"comment1", "comment2"}, value.comment());
	}

	// --- constant() tests ---

	@Test
	void constant_setsConstantValue() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);
		value.constant("fixed");

		assertTrue(value.constant().isPresent());
		assertEquals("fixed", value.constant().get());
	}

	@Test
	void constant_initiallyEmpty() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);

		assertTrue(value.constant().isEmpty());
	}

	// --- required() tests ---

	@Test
	void required_setsRequiredValue() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);
		value.required(() -> "default");

		assertTrue(value.required().isPresent());
		assertEquals("default", value.required().get().get());
	}

	@Test
	void required_initiallyEmpty() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);

		assertTrue(value.required().isEmpty());
	}

	// --- with() tests ---

	@Test
	void with_setsDefaultValue() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);
		value.with(() -> "default");

		assertTrue(value.with().isPresent());
		assertEquals("default", value.with().get().get());
	}

	@Test
	void with_initiallyEmpty() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);

		assertTrue(value.with().isEmpty());
	}

	// --- validator() tests ---

	@Test
	void validator_setsValidator() {
		ConfigurationValueWriter<Integer> value = new ConfigurationValueWriter<>("port", new String[]{},
				ConfigurationValueWriter.Type.INT);
		value.validator(obj -> obj instanceof Integer && (Integer) obj > 0);

		assertTrue(value.validator().isPresent());
	}

	@Test
	void validator_initiallyEmpty() {
		ConfigurationValueWriter<Integer> value = new ConfigurationValueWriter<>("port", new String[]{},
				ConfigurationValueWriter.Type.INT);

		assertTrue(value.validator().isEmpty());
	}

	// --- resolve() tests ---
	// resolve() calls type.apply() which returns the parsed value

	@Test
	void resolve_STRING_type() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);
		ScalarNode scalar = new ScalarNode(Tag.STR, "hello", null, null, ScalarStyle.PLAIN);

		Object result = value.resolve(scalar);
		assertEquals("hello", result);
	}

	@Test
	void resolve_INT_type() {
		ConfigurationValueWriter<Integer> value = new ConfigurationValueWriter<>("count", new String[]{},
				ConfigurationValueWriter.Type.INT);
		ScalarNode scalar = new ScalarNode(Tag.INT, "42", null, null, ScalarStyle.PLAIN);

		Object result = value.resolve(scalar);
		assertEquals(42, result);
	}

	@Test
	void resolve_LONG_type() {
		ConfigurationValueWriter<Long> value = new ConfigurationValueWriter<>("big", new String[]{},
				ConfigurationValueWriter.Type.LONG);
		ScalarNode scalar = new ScalarNode(Tag.INT, "9223372036854775807", null, null,
				ScalarStyle.PLAIN);

		Object result = value.resolve(scalar);
		assertEquals(9223372036854775807L, result);
	}

	@Test
	void resolve_FLOAT_type() {
		ConfigurationValueWriter<Float> value = new ConfigurationValueWriter<>("pi", new String[]{},
				ConfigurationValueWriter.Type.FLOAT);
		ScalarNode scalar = new ScalarNode(Tag.FLOAT, "3.14", null, null, ScalarStyle.PLAIN);

		Object result = value.resolve(scalar);
		assertEquals(3.14f, result);
	}

	@Test
	void resolve_DOUBLE_type() {
		ConfigurationValueWriter<Double> value = new ConfigurationValueWriter<>("pi", new String[]{},
				ConfigurationValueWriter.Type.DOUBLE);
		ScalarNode scalar = new ScalarNode(Tag.FLOAT, "3.14159", null, null, ScalarStyle.PLAIN);

		Object result = value.resolve(scalar);
		assertEquals(3.14159, result);
	}

	@Test
	void resolve_BOOLEAN_type() {
		ConfigurationValueWriter<Boolean> value = new ConfigurationValueWriter<>("flag", new String[]{},
				ConfigurationValueWriter.Type.BOOLEAN);
		ScalarNode scalar = new ScalarNode(Tag.BOOL, "true", null, null, ScalarStyle.PLAIN);

		Object result = value.resolve(scalar);
		assertEquals(true, result);
	}

	@Test
	void resolve_invalid_INT_throwsRuntimeException() {
		ConfigurationValueWriter<Integer> value = new ConfigurationValueWriter<>("count", new String[]{},
				ConfigurationValueWriter.Type.INT);
		ScalarNode scalar = new ScalarNode(Tag.STR, "not a number", null, null, ScalarStyle.PLAIN);

		assertThrows(RuntimeException.class, () -> value.resolve(scalar));
	}

	// --- Combined fluent API tests ---

	@Test
	void fluentAPI_constant_returnsThis() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);

		assertSame(value, value.constant("fixed"));
	}

	@Test
	void fluentAPI_required_returnsThis() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);

		assertSame(value, value.required(() -> "default"));
	}

	@Test
	void fluentAPI_with_returnsThis() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);

		assertSame(value, value.with(() -> "default"));
	}

	@Test
	void fluentAPI_validator_returnsThis() {
		ConfigurationValueWriter<Integer> value = new ConfigurationValueWriter<>("port", new String[]{},
				ConfigurationValueWriter.Type.INT);

		assertSame(value, value.validator(_ -> true));
	}

	@Test
	void fluentAPI_combinedConfiguration() {
		ConfigurationValueWriter<Integer> value = new ConfigurationValueWriter<>("port", new String[]{"Port number"},
				ConfigurationValueWriter.Type.INT);

		value.required(() -> 8080);
		value.validator(obj -> obj instanceof Integer && (Integer) obj > 0);

		assertEquals("port", value.key());
		assertArrayEquals(new String[]{"Port number"}, value.comment());
		assertEquals(ConfigurationValueWriter.Type.INT, value.type());
		assertTrue(value.required().isPresent());
		assertTrue(value.validator().isPresent());
	}

	@Test
	void constant_setsAllFields() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);
		value.constant("fixed");

		// constant() sets constant, required, default, and validator
		assertTrue(value.constant().isPresent());
		assertTrue(value.required().isPresent());
		assertTrue(value.with().isPresent());
		assertTrue(value.validator().isPresent());
	}

	@Test
	void required_afterWith_updatesRequired() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);
		value.with(() -> "initial");
		value.required(() -> "required-default");

		assertTrue(value.required().isPresent());
		assertEquals("required-default", value.required().get().get());
		// required() also updates defaultValue if with() was called first
		assertTrue(value.with().isPresent());
		assertEquals("required-default", value.with().get().get());
	}

	@Test
	void with_afterRequired_clearsConstant() {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>("name", new String[]{},
				ConfigurationValueWriter.Type.STRING);
		value.constant("fixed");
		value.with(() -> "new-default");

		// with() clears constant
		assertTrue(value.constant().isEmpty());
		assertTrue(value.with().isPresent());
		assertEquals("new-default", value.with().get().get());
	}
}
