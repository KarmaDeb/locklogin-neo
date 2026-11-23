package com.github.karmadeb.locklogin.neo.common.i18n.bundle;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.nodes.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationBundleTest {

	private MappingNode createNode(String key, String value) {
		MappingNode mapping = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ScalarNode keyNode = new ScalarNode(Tag.STR, key, null, null, ScalarStyle.PLAIN);
		ScalarNode valueNode = new ScalarNode(Tag.STR, value, null, null, ScalarStyle.SINGLE_QUOTED);
		mapping.getValue().add(new NodeTuple(keyNode, valueNode));
		return mapping;
	}

	// --- Constructor tests ---

	@Test
	void constructor_emptyReaders_createsEmptyBundle() {
		LocalizationBundle bundle = new LocalizationBundle(Collections.emptyMap());

		assertTrue(bundle.locales().isEmpty());
	}

	@Test
	void constructor_withReaders_populatesLocales() {
		MappingNode enNode = createNode("greeting", "Hello");
		MappingNode frNode = createNode("greeting", "Bonjour");

		Map<Locale, LocalizationReader> readers = new LinkedHashMap<>();
		readers.put(Locale.ENGLISH, new LocalizationReader(Locale.ENGLISH, enNode));
		readers.put(Locale.FRENCH, new LocalizationReader(Locale.FRENCH, frNode));

		LocalizationBundle bundle = new LocalizationBundle(readers);

		assertEquals(2, bundle.locales().size());
		assertTrue(bundle.locales().contains(Locale.ENGLISH));
		assertTrue(bundle.locales().contains(Locale.FRENCH));
	}

	// --- locales() tests ---

	@Test
	void locales_returnsUnmodifiableCollection() {
		LocalizationBundle bundle = new LocalizationBundle(Collections.emptyMap());

		// noinspection DataFlowIssue
		assertThrows(UnsupportedOperationException.class, () -> bundle.locales().add(Locale.ENGLISH));
	}

	@Test
	void locales_returnsAllConfiguredLocales() {
		Map<Locale, LocalizationReader> readers = new LinkedHashMap<>();
		readers.put(Locale.ENGLISH, new LocalizationReader(Locale.ENGLISH, new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO)));
		readers.put(Locale.FRENCH, new LocalizationReader(Locale.FRENCH, new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO)));
		readers.put(Locale.of("es"), new LocalizationReader(Locale.of("es"), new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO)));

		LocalizationBundle bundle = new LocalizationBundle(readers);

		assertEquals(3, bundle.locales().size());
	}

	// --- reader() tests ---

	@Test
	void reader_existingLocale_returnsReader() {
		MappingNode node = createNode("key", "value");
		Map<Locale, LocalizationReader> readers = new LinkedHashMap<>();
		readers.put(Locale.ENGLISH, new LocalizationReader(Locale.ENGLISH, node));

		LocalizationBundle bundle = new LocalizationBundle(readers);

		var reader = bundle.reader(Locale.ENGLISH);
		assertNotNull(reader);
		assertEquals(Locale.ENGLISH, reader.locale());
	}

	@Test
	void reader_missingLocale_returnsEmptyReader() {
		LocalizationBundle bundle = new LocalizationBundle(Collections.emptyMap());

		var reader = bundle.reader(Locale.ENGLISH);
		assertNotNull(reader);
		assertEquals(Locale.ENGLISH, reader.locale());
	}

	// --- writer() tests ---

	@Test
	void writer_existingLocale_returnsWriter() {
		MappingNode node = createNode("key", "value");
		Map<Locale, LocalizationReader> readers = new LinkedHashMap<>();
		readers.put(Locale.ENGLISH, new LocalizationReader(Locale.ENGLISH, node));

		LocalizationBundle bundle = new LocalizationBundle(readers);

		var writer = bundle.writer(Locale.ENGLISH);
		assertNotNull(writer);
		assertEquals(Locale.ENGLISH, writer.locale());
	}

	@Test
	void writer_missingLocale_createsNewWriter() {
		LocalizationBundle bundle = new LocalizationBundle(Collections.emptyMap());

		var writer = bundle.writer(Locale.of("de"));
		assertNotNull(writer);
		assertEquals(Locale.of("de"), writer.locale());
	}

	@Test
	void writer_newLocale_addsToLocales() {
		LocalizationBundle bundle = new LocalizationBundle(Collections.emptyMap());

		assertTrue(bundle.locales().isEmpty());
		bundle.writer(Locale.of("it"));
		assertTrue(bundle.locales().contains(Locale.of("it")));
	}

	@Test
	void writer_returnsSameInstance() {
		LocalizationBundle bundle = new LocalizationBundle(Collections.emptyMap());

		var writer1 = bundle.writer(Locale.ENGLISH);
		var writer2 = bundle.writer(Locale.ENGLISH);
		assertSame(writer1, writer2);
	}

	// --- compose() tests ---

	@Test
	void compose_copiesKeysAcrossLocales() {
		MappingNode enNode = createNode("greeting", "Hello");
		Map<Locale, LocalizationReader> readers = new LinkedHashMap<>();
		readers.put(Locale.ENGLISH, new LocalizationReader(Locale.ENGLISH, enNode));
		readers.put(Locale.FRENCH, new LocalizationReader(Locale.FRENCH, new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO)));

		LocalizationBundle bundle = new LocalizationBundle(readers);
		bundle.compose();

		// French should have received the English key
		var frReader = bundle.reader(Locale.FRENCH);
		assertTrue(frReader.defined("greeting"));
		assertEquals("Hello", frReader.find("greeting").get()[0].message());
	}

	@Test
	void compose_preservesExistingTranslations() {
		MappingNode enNode = createNode("greeting", "Hello");
		MappingNode frNode = createNode("greeting", "Bonjour");
		Map<Locale, LocalizationReader> readers = new LinkedHashMap<>();
		readers.put(Locale.ENGLISH, new LocalizationReader(Locale.ENGLISH, enNode));
		readers.put(Locale.FRENCH, new LocalizationReader(Locale.FRENCH, frNode));

		LocalizationBundle bundle = new LocalizationBundle(readers);
		bundle.compose();

		// French should keep its own translation
		var frReader = bundle.reader(Locale.FRENCH);
		assertEquals("Bonjour", frReader.find("greeting").get()[0].message());
	}

	@Test
	void compose_withThreeLocales_spreadsAllKeys() {
		MappingNode enNode = createNode("key1", "English");
		MappingNode frNode = createNode("key2", "Francais");
		MappingNode esNode = createNode("key3", "Espanol");

		Map<Locale, LocalizationReader> readers = new LinkedHashMap<>();
		readers.put(Locale.ENGLISH, new LocalizationReader(Locale.ENGLISH, enNode));
		readers.put(Locale.FRENCH, new LocalizationReader(Locale.FRENCH, frNode));
		readers.put(Locale.of("es"), new LocalizationReader(Locale.of("es"), esNode));

		LocalizationBundle bundle = new LocalizationBundle(readers);
		bundle.compose();

		// Each locale should have all keys
		for (Locale locale : bundle.locales()) {
			var reader = bundle.reader(locale);
			assertTrue(reader.defined("key1"));
			assertTrue(reader.defined("key2"));
			assertTrue(reader.defined("key3"));
		}
	}

	@Test
	void compose_emptyBundle_doesNothing() {
		LocalizationBundle bundle = new LocalizationBundle(Collections.emptyMap());
		bundle.compose();
		// Should not throw
		assertTrue(bundle.locales().isEmpty());
	}

	@Test
	void compose_singleLocale_noChange() {
		MappingNode enNode = createNode("greeting", "Hello");
		Map<Locale, LocalizationReader> readers = new LinkedHashMap<>();
		readers.put(Locale.ENGLISH, new LocalizationReader(Locale.ENGLISH, enNode));

		LocalizationBundle bundle = new LocalizationBundle(readers);
		bundle.compose();

		var reader = bundle.reader(Locale.ENGLISH);
		assertEquals("Hello", reader.find("greeting").get()[0].message());
	}

	// --- Write and compose workflow ---

	@Test
	void writer_thenCompose_propagatesNewKeys() {
		LocalizationBundle bundle = new LocalizationBundle(Collections.emptyMap());

		var enWriter = bundle.writer(Locale.ENGLISH);
		enWriter.write("title", "Welcome");

		var frWriter = bundle.writer(Locale.FRENCH);
		// French has no keys yet

		bundle.compose();

		// French should now have the title key from English
		var frReader = bundle.reader(Locale.FRENCH);
		assertTrue(frReader.defined("title"));
		assertEquals("Welcome", frReader.find("title").get()[0].message());
	}

	@Test
	void writer_overwritesInOneLocale_only() {
		MappingNode enNode = createNode("greeting", "Hello");
		MappingNode frNode = createNode("greeting", "Bonjour");

		Map<Locale, LocalizationReader> readers = new LinkedHashMap<>();
		readers.put(Locale.ENGLISH, new LocalizationReader(Locale.ENGLISH, enNode));
		readers.put(Locale.FRENCH, new LocalizationReader(Locale.FRENCH, frNode));

		LocalizationBundle bundle = new LocalizationBundle(readers);

		// write() on an existing key adds a duplicate tuple; find() returns the first one
		var enWriter = bundle.writer(Locale.ENGLISH);
		enWriter.write("greeting", "Hi there");

		bundle.compose();

		// English keeps the original value (first tuple wins)
		var enReader = bundle.reader(Locale.ENGLISH);
		assertEquals("Hello", enReader.find("greeting").get()[0].message());

		// French should keep its own
		var frReader = bundle.reader(Locale.FRENCH);
		assertEquals("Bonjour", frReader.find("greeting").get()[0].message());
	}

	// --- Key ordering tests ---

	@Test
	void compose_reordersKeysToMatchFirstLocale() {
		// English has keys in order: a, b, c
		MappingNode enNode = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		enNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "a", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "A English", null, null, ScalarStyle.SINGLE_QUOTED)));
		enNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "b", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "B English", null, null, ScalarStyle.SINGLE_QUOTED)));
		enNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "c", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "C English", null, null, ScalarStyle.SINGLE_QUOTED)));

		// French has keys in a different order: c, a
		MappingNode frNode = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		frNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "c", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "C Francais", null, null, ScalarStyle.SINGLE_QUOTED)));
		frNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "a", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "A Francais", null, null, ScalarStyle.SINGLE_QUOTED)));

		Map<Locale, LocalizationReader> readers = new LinkedHashMap<>();
		readers.put(Locale.ENGLISH, new LocalizationReader(Locale.ENGLISH, enNode));
		readers.put(Locale.FRENCH, new LocalizationReader(Locale.FRENCH, frNode));

		LocalizationBundle bundle = new LocalizationBundle(readers);
		bundle.compose();

		// French keys should now be ordered: a, b, c (matching English)
		List<String> frKeys = new ArrayList<>();
		for (var tuple : bundle.reader(Locale.FRENCH).yaml().getValue()) {
			if (tuple.getKeyNode() instanceof ScalarNode s) {
				frKeys.add(s.getValue());
			}
		}
		assertEquals(List.of("a", "b", "c"), frKeys);

		// Values should be preserved
		var frReader = bundle.reader(Locale.FRENCH);
		assertEquals("A Francais", frReader.find("a").get()[0].message());
		assertEquals("C Francais", frReader.find("c").get()[0].message());
		// "b" was copied from English
		assertEquals("B English", frReader.find("b").get()[0].message());
	}

	@Test
	void compose_reordersNestedKeysToMatchFirstLocale() {
		// English has nested keys in order: ui.title, ui.subtitle
		MappingNode enNested = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		enNested.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "ui.title", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "Title EN", null, null, ScalarStyle.SINGLE_QUOTED)));
		enNested.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "ui.subtitle", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "Subtitle EN", null, null, ScalarStyle.SINGLE_QUOTED)));

		MappingNode enNode = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		enNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "ui", null, null, ScalarStyle.PLAIN), enNested));

		// French has nested keys in reverse order: ui.subtitle, ui.title
		MappingNode frNested = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		frNested.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "ui.subtitle", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "Sous-titre FR", null, null, ScalarStyle.SINGLE_QUOTED)));
		frNested.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "ui.title", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "Titre FR", null, null, ScalarStyle.SINGLE_QUOTED)));

		MappingNode frNode = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		frNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "ui", null, null, ScalarStyle.PLAIN), frNested));

		Map<Locale, LocalizationReader> readers = new LinkedHashMap<>();
		readers.put(Locale.ENGLISH, new LocalizationReader(Locale.ENGLISH, enNode));
		readers.put(Locale.FRENCH, new LocalizationReader(Locale.FRENCH, frNode));

		LocalizationBundle bundle = new LocalizationBundle(readers);
		bundle.compose();

		// French nested keys should be reordered to match English: title, subtitle
		var frReader = bundle.reader(Locale.FRENCH);
		MappingNode frUiMapping = (MappingNode) frReader.resolver.resolve("ui").orElseThrow();
		List<String> frNestedKeys = new ArrayList<>();
		for (var tuple : frUiMapping.getValue()) {
			if (tuple.getKeyNode() instanceof ScalarNode s) {
				frNestedKeys.add(s.getValue());
			}
		}
		assertEquals(List.of("ui.title", "ui.subtitle"), frNestedKeys);

		// Values should be preserved
		assertEquals("Titre FR", frReader.find("ui.title").get()[0].message());
		assertEquals("Sous-titre FR", frReader.find("ui.subtitle").get()[0].message());
	}

	@Test
	void compose_singleLocale_preservesOrder() {
		MappingNode enNode = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		enNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "z", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "Z", null, null, ScalarStyle.SINGLE_QUOTED)));
		enNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "a", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "A", null, null, ScalarStyle.SINGLE_QUOTED)));

		Map<Locale, LocalizationReader> readers = new LinkedHashMap<>();
		readers.put(Locale.ENGLISH, new LocalizationReader(Locale.ENGLISH, enNode));

		LocalizationBundle bundle = new LocalizationBundle(readers);
		bundle.compose();

		// Order should be preserved: z, a
		List<String> keys = new ArrayList<>();
		for (var tuple : bundle.reader(Locale.ENGLISH).yaml().getValue()) {
			if (tuple.getKeyNode() instanceof ScalarNode s) {
				keys.add(s.getValue());
			}
		}
		assertEquals(List.of("z", "a"), keys);
	}

	@Test
	void compose_threeLocales_allMatchFirstLocaleOrder() {
		// English (first): a, b, c
		MappingNode enNode = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		enNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "a", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "A EN", null, null, ScalarStyle.SINGLE_QUOTED)));
		enNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "b", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "B EN", null, null, ScalarStyle.SINGLE_QUOTED)));
		enNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "c", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "C EN", null, null, ScalarStyle.SINGLE_QUOTED)));

		// French: c, b
		MappingNode frNode = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		frNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "c", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "C FR", null, null, ScalarStyle.SINGLE_QUOTED)));
		frNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "b", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "B FR", null, null, ScalarStyle.SINGLE_QUOTED)));

		// Spanish: b, a
		MappingNode esNode = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		esNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "b", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "B ES", null, null, ScalarStyle.SINGLE_QUOTED)));
		esNode.getValue().add(new NodeTuple(
			new ScalarNode(Tag.STR, "a", null, null, ScalarStyle.PLAIN),
			new ScalarNode(Tag.STR, "A ES", null, null, ScalarStyle.SINGLE_QUOTED)));

		Map<Locale, LocalizationReader> readers = new LinkedHashMap<>();
		readers.put(Locale.ENGLISH, new LocalizationReader(Locale.ENGLISH, enNode));
		readers.put(Locale.FRENCH, new LocalizationReader(Locale.FRENCH, frNode));
		readers.put(Locale.of("es"), new LocalizationReader(Locale.of("es"), esNode));

		LocalizationBundle bundle = new LocalizationBundle(readers);
		bundle.compose();

		// All locales should have keys in order: a, b, c
		for (Locale locale : bundle.locales()) {
			List<String> keys = new ArrayList<>();
			for (var tuple : bundle.reader(locale).yaml().getValue()) {
				if (tuple.getKeyNode() instanceof ScalarNode s) {
					keys.add(s.getValue());
				}
			}
			assertEquals(List.of("a", "b", "c"), keys, "Locale " + locale + " should have keys in order a, b, c");
		}
	}
}
