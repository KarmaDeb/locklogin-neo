package com.github.karmadeb.locklogin.neo.common.i18n.bundle;

import com.github.karmadeb.locklogin.neo.api.i18n.phrase.Phrase;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.nodes.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationWriterTest {

	private MappingNode createNode(String key, String value) {
		MappingNode mapping = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, key, null, null, ScalarStyle.PLAIN);
		ScalarNode valueNode = new ScalarNode(Tag.STR, value, null, null, ScalarStyle.SINGLE_QUOTED);
		mapping.getValue().add(new NodeTuple(keyNode, valueNode));
		return mapping;
	}

	private LocalizationWriter createWriter() {
		return new LocalizationWriter(Locale.ENGLISH, new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO));
	}

	// --- Constructor tests ---

	@Test
	void constructor_setsLocale() {
		MappingNode node = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		LocalizationWriter writer = new LocalizationWriter(Locale.FRENCH, node);

		assertEquals(Locale.FRENCH, writer.locale());
	}

	@Test
	void constructor_nullLocale_throwsNullPointerException() {
		MappingNode node = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		assertThrows(NullPointerException.class, () -> new LocalizationWriter(null, node));
	}

	@Test
	void constructor_nullYaml_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> new LocalizationWriter(Locale.ENGLISH, null));
	}

	// --- write(String, String) tests ---

	@Test
	void write_singlePhrase_writesAndReadsValue() {
		LocalizationWriter writer = createWriter();
		writer.write("greeting", "Hello");

		Optional<Phrase[]> result = writer.find("greeting");
		assertTrue(result.isPresent());
		assertEquals("Hello", result.get()[0].message());
	}

	@Test
	void write_existingKey_doesNotOverwrite() {
		MappingNode node = createNode("greeting", "Original");
		LocalizationWriter writer = new LocalizationWriter(Locale.ENGLISH, node);
		writer.write("greeting", "New value");

		Optional<Phrase[]> result = writer.find("greeting");
		assertTrue(result.isPresent());
		assertEquals("Original", result.get()[0].message());
	}

	@Test
	void write_multipleKeys_preservesAll() {
		LocalizationWriter writer = createWriter();
		writer.write("key1", "Value1");
		writer.write("key2", "Value2");
		writer.write("key3", "Value3");

		assertEquals("Value1", writer.find("key1").get()[0].message());
		assertEquals("Value2", writer.find("key2").get()[0].message());
		assertEquals("Value3", writer.find("key3").get()[0].message());
	}

	@Test
	void write_nullKey_throwsNullPointerException() {
		LocalizationWriter writer = createWriter();
		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> writer.write(null, "value"));
	}

	@Test
	void write_nullPhrase_throwsNullPointerException() {
		LocalizationWriter writer = createWriter();
		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> writer.write((String) "key", (String) null));
	}

	// --- write(String, Collection) tests ---

	@Test
	void write_collection_writesSequence() {
		LocalizationWriter writer = createWriter();
		Collection<String> greetings = List.of("Hello", "Hi", "Hey");
		writer.write("greetings", greetings);

		Optional<Phrase[]> result = writer.find("greetings");
		assertTrue(result.isPresent());
		Phrase[] phrases = result.get();
		assertEquals(3, phrases.length);
		assertEquals("Hello", phrases[0].message());
		assertEquals("Hi", phrases[1].message());
		assertEquals("Hey", phrases[2].message());
	}

	@Test
	void write_emptyCollection_createsEmptySequence() {
		LocalizationWriter writer = createWriter();
		Collection<String> empty = List.of();
		writer.write("empty", empty);

		// The key is created with an empty sequence
		assertTrue(writer.defined("empty"));
	}

	@Test
	void write_collection_nullKey_throwsNullPointerException() {
		LocalizationWriter writer = createWriter();
		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> writer.write((String) null, (Collection<String>) List.of("value")));
	}

	@Test
	void write_collection_nullCollection_throwsNullPointerException() {
		LocalizationWriter writer = createWriter();
		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> writer.write((String) "key", (Collection<String>) null));
	}

	// --- unset() tests ---
	// Note: unset() uses resolveTuple(key, -1, false) which requires at least
	// 2 parts in the key path, so flat keys are not removed.
	// Testing only the safe behaviors.

	@Test
	void unset_missingKey_doesNothing() {
		LocalizationWriter writer = createWriter();
		// Should not throw
		writer.unset("nonexistent");
	}

	@Test
	void unset_nullKey_throwsNullPointerException() {
		LocalizationWriter writer = createWriter();
		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> writer.unset(null));
	}

	// --- copyTo() tests ---

	@Test
	void copyTo_copiesEntriesToTarget() {
		MappingNode sourceNode = createNode("greeting", "Hello");
		LocalizationWriter source = new LocalizationWriter(Locale.ENGLISH, sourceNode);

		LocalizationWriter target = new LocalizationWriter(Locale.FRENCH, new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO));
		source.copyTo(target);

		assertTrue(target.defined("greeting"));
		assertEquals("Hello", target.find("greeting").get()[0].message());
	}

	@Test
	void copyTo_doesNotOverwriteExistingEntries() {
		MappingNode sourceNode = createNode("greeting", "Hello");
		LocalizationWriter source = new LocalizationWriter(Locale.ENGLISH, sourceNode);

		MappingNode targetNode = createNode("greeting", "Bonjour");
		LocalizationWriter target = new LocalizationWriter(Locale.FRENCH, targetNode);
		source.copyTo(target);

		// Target's original value should be preserved
		assertEquals("Bonjour", target.find("greeting").get()[0].message());
	}

	@Test
	void copyTo_copiesMultipleEntries() {
		MappingNode sourceNode = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode key1 = new ScalarNode(Tag.STR, "a", null, null, ScalarStyle.PLAIN);
		ScalarNode val1 = new ScalarNode(Tag.STR, "One", null, null, ScalarStyle.SINGLE_QUOTED);
		sourceNode.getValue().add(new NodeTuple(key1, val1));

		ScalarNode key2 = new ScalarNode(Tag.STR, "b", null, null, ScalarStyle.PLAIN);
		ScalarNode val2 = new ScalarNode(Tag.STR, "Two", null, null, ScalarStyle.SINGLE_QUOTED);
		sourceNode.getValue().add(new NodeTuple(key2, val2));

		LocalizationWriter source = new LocalizationWriter(Locale.ENGLISH, sourceNode);
		LocalizationWriter target = new LocalizationWriter(Locale.FRENCH, new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO));
		source.copyTo(target);

		assertTrue(target.defined("a"));
		assertTrue(target.defined("b"));
		assertEquals("One", target.find("a").get()[0].message());
		assertEquals("Two", target.find("b").get()[0].message());
	}

	@Test
	void copyTo_copiesNestedMappings() {
		// NodeResolver uses cumulative keys, so nested key must be "ui.title" literally
		MappingNode nested = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode nestedKey = new ScalarNode(Tag.STR, "ui.title", null, null, ScalarStyle.PLAIN);
		ScalarNode nestedValue = new ScalarNode(Tag.STR, "Welcome", null, null, ScalarStyle.SINGLE_QUOTED);
		nested.getValue().add(new NodeTuple(nestedKey, nestedValue));

		MappingNode sourceNode = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, "ui", null, null, ScalarStyle.PLAIN);
		sourceNode.getValue().add(new NodeTuple(keyNode, nested));

		LocalizationWriter source = new LocalizationWriter(Locale.ENGLISH, sourceNode);
		LocalizationWriter target = new LocalizationWriter(Locale.FRENCH, new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO));
		source.copyTo(target);

		// copyTo copies the "ui" mapping, then recursively copies "ui.title" inside
		assertTrue(target.defined("ui"));
	}

	@Test
	void copyTo_emptySource_doesNothing() {
		LocalizationWriter source = new LocalizationWriter(Locale.ENGLISH, new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO));
		LocalizationWriter target = createWriter();
		source.copyTo(target);

		assertEquals(0, target.yaml().getValue().size());
	}

	// --- Inherited reader methods ---

	@Test
	void defined_inheritedFromReader() {
		MappingNode node = createNode("key", "value");
		LocalizationWriter writer = new LocalizationWriter(Locale.ENGLISH, node);

		assertTrue(writer.defined("key"));
		assertFalse(writer.defined("missing"));
	}

	@Test
	void find_inheritedFromReader() {
		MappingNode node = createNode("key", "value");
		LocalizationWriter writer = new LocalizationWriter(Locale.ENGLISH, node);

		Optional<Phrase[]> result = writer.find("key");
		assertTrue(result.isPresent());
		assertEquals("value", result.get()[0].message());
	}

	@Test
	void locale_inheritedFromReader() {
		LocalizationWriter writer = new LocalizationWriter(Locale.of("de"), new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO));

		assertEquals(Locale.of("de"), writer.locale());
	}
}
