package com.github.karmadeb.locklogin.neo.common.i18n.bundle;

import com.github.karmadeb.locklogin.neo.api.i18n.phrase.Phrase;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationReaderTest {

	private MappingNode createNode(String key, String value) {
		MappingNode mapping = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, key, null, null, ScalarStyle.PLAIN);
		ScalarNode valueNode = new ScalarNode(Tag.STR, value, null, null, ScalarStyle.SINGLE_QUOTED);
		mapping.getValue().add(new NodeTuple(keyNode, valueNode));
		return mapping;
	}

	private LocalizationReader createReader() {
		return new LocalizationReader(Locale.ENGLISH, new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO));
	}

	// --- Constructor tests ---

	@Test
	void constructor_setsLocale() {
		MappingNode node = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		LocalizationReader reader = new LocalizationReader(Locale.FRENCH, node);

		assertEquals(Locale.FRENCH, reader.locale());
	}

	@Test
	void constructor_nullLocale_throwsNullPointerException() {
		MappingNode node = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		assertThrows(NullPointerException.class, () -> new LocalizationReader(null, node));
	}

	@Test
	void constructor_nullYaml_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> new LocalizationReader(Locale.ENGLISH, null));
	}

	// --- yaml() accessor ---

	@Test
	void yaml_returnsUnderlyingNode() {
		MappingNode node = createNode("key", "value");
		LocalizationReader reader = new LocalizationReader(Locale.ENGLISH, node);

		assertSame(node, reader.yaml());
	}

	// --- defined() tests ---

	@Test
	void defined_existingKey_returnsTrue() {
		MappingNode node = createNode("greeting", "Hello");
		LocalizationReader reader = new LocalizationReader(Locale.ENGLISH, node);

		assertTrue(reader.defined("greeting"));
	}

	@Test
	void defined_missingKey_returnsFalse() {
		LocalizationReader reader = createReader();

		assertFalse(reader.defined("missing"));
	}

	@Test
	void defined_nestedKey_returnsTrue() {
		// NodeResolver builds cumulative keys: for "hello.world", it looks for
		// "hello" first, then "hello.world" in the nested mapping.
		// So we need a key literally named "hello.world" in the nested mapping.
		MappingNode nested = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode nestedKey = new ScalarNode(Tag.STR, "hello.world", null, null, ScalarStyle.PLAIN);
		ScalarNode nestedValue = new ScalarNode(Tag.STR, "Bonjour", null, null, ScalarStyle.SINGLE_QUOTED);
		nested.getValue().add(new NodeTuple(nestedKey, nestedValue));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, "hello", null, null, ScalarStyle.PLAIN);
		root.getValue().add(new NodeTuple(keyNode, nested));

		LocalizationReader reader = new LocalizationReader(Locale.FRENCH, root);

		assertTrue(reader.defined("hello.world"));
	}

	@Test
	void defined_nestedKey_missing_returnsFalse() {
		LocalizationReader reader = createReader();

		assertFalse(reader.defined("a.b.c"));
	}

	// --- find() tests ---

	@Test
	void find_scalarValue_returnsPhrase() {
		MappingNode node = createNode("greeting", "Hello world");
		LocalizationReader reader = new LocalizationReader(Locale.ENGLISH, node);

		Optional<Phrase[]> result = reader.find("greeting");
		assertTrue(result.isPresent());
		Phrase[] phrases = result.get();
		assertEquals(1, phrases.length);
		assertEquals("Hello world", phrases[0].message());
		assertEquals(Locale.ENGLISH, phrases[0].locale());
	}

	@Test
	void find_missingKey_returnsEmpty() {
		LocalizationReader reader = createReader();

		Optional<Phrase[]> result = reader.find("missing");
		assertTrue(result.isEmpty());
	}

	@Test
	void find_sequenceValue_returnsMultiplePhrases() {
		ScalarNode item1 = new ScalarNode(Tag.STR, "Hello", null, null, ScalarStyle.SINGLE_QUOTED);
		ScalarNode item2 = new ScalarNode(Tag.STR, "Hi", null, null, ScalarStyle.SINGLE_QUOTED);
		ScalarNode item3 = new ScalarNode(Tag.STR, "Hey", null, null, ScalarStyle.SINGLE_QUOTED);
		SequenceNode seq = new SequenceNode(Tag.SEQ, List.of(item1, item2, item3), FlowStyle.BLOCK);

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, "greetings", null, null, ScalarStyle.PLAIN);
		root.getValue().add(new NodeTuple(keyNode, seq));

		LocalizationReader reader = new LocalizationReader(Locale.ENGLISH, root);

		Optional<Phrase[]> result = reader.find("greetings");
		assertTrue(result.isPresent());
		Phrase[] phrases = result.get();
		assertEquals(3, phrases.length);
		assertEquals("Hello", phrases[0].message());
		assertEquals("Hi", phrases[1].message());
		assertEquals("Hey", phrases[2].message());
	}

	@Test
	void find_nestedKey_returnsPhrase() {
		// Same cumulative key pattern: need "ui.title" as the literal key in nested mapping
		MappingNode nested = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode nestedKey = new ScalarNode(Tag.STR, "ui.title", null, null, ScalarStyle.PLAIN);
		ScalarNode nestedValue = new ScalarNode(Tag.STR, "Welcome", null, null, ScalarStyle.SINGLE_QUOTED);
		nested.getValue().add(new NodeTuple(nestedKey, nestedValue));

		MappingNode root = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, "ui", null, null, ScalarStyle.PLAIN);
		root.getValue().add(new NodeTuple(keyNode, nested));

		LocalizationReader reader = new LocalizationReader(Locale.ENGLISH, root);

		Optional<Phrase[]> result = reader.find("ui.title");
		assertTrue(result.isPresent());
		assertEquals("Welcome", result.get()[0].message());
	}

	// --- get() default tests ---

	@Test
	void get_existingKey_returnsPhrase() {
		MappingNode node = createNode("greeting", "Hello");
		LocalizationReader reader = new LocalizationReader(Locale.ENGLISH, node);

		Phrase[] result = reader.get("greeting", "Default");
		assertEquals(1, result.length);
		assertEquals("Hello", result[0].message());
	}

	@Test
	void get_missingKey_returnsDefault() {
		LocalizationReader reader = createReader();

		Phrase[] result = reader.get("missing", "Default value");
		assertEquals(1, result.length);
		assertEquals("Default value", result[0].message());
		assertEquals(Locale.ENGLISH, result[0].locale());
	}

	@Test
	void get_missingKey_returnsMultipleDefaults() {
		LocalizationReader reader = createReader();

		Phrase[] result = reader.get("missing", "Default1", "Default2");
		assertEquals(2, result.length);
		assertEquals("Default1", result[0].message());
		assertEquals("Default2", result[1].message());
	}

	@Test
	void get_missingKey_returnsCollectionDefaults() {
		LocalizationReader reader = createReader();

		Phrase[] result = reader.get("missing", List.of("First", "Second", "Third"));
		assertEquals(3, result.length);
		assertEquals("First", result[0].message());
		assertEquals("Second", result[1].message());
		assertEquals("Third", result[2].message());
	}

	@Test
	void get_emptyDefaults_returnsEmptyArray() {
		LocalizationReader reader = createReader();

		Phrase[] result = reader.get("missing");
		assertEquals(0, result.length);
	}
}
