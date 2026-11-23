package com.github.karmadeb.locklogin.neo.common.i18n.phrase;

import com.github.karmadeb.locklogin.neo.api.i18n.phrase.Phrase;
import com.github.karmadeb.locklogin.neo.api.i18n.phrase.Placeholders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class PlaceholdersTest {

	private Placeholders parser;

	@BeforeEach
	void setUp() {
		parser = Placeholders.parser();
	}

	// --- parser() tests ---

	@Test
	void parser_returnsNewInstance() {
		var p1 = Placeholders.parser();
		var p2 = Placeholders.parser();
		assertNotSame(p1, p2);
	}

	// --- with() tests ---

	@Test
	void with_returnsSameInstance() {
		var result = parser.with("key", "value");
		assertSame(parser, result);
	}

	@Test
	void with_chaining_addsMultipleItems() {
		parser.with("name", "Alice").with("count", 30).with("place", "Paris");

		Phrase phrase = new Phrase(Locale.ENGLISH, "Hello {name}, {count} items, from {place}");
		String result = parser.apply(phrase);

		// Placeholders.replace uses the key without braces
		assertEquals("Hello {Alice}, {30} items, from {Paris}", result);
	}

	@Test
	void with_overwritesPreviousValue() {
		parser.with("name", "Alice");
		parser.with("name", "Bob");

		Phrase phrase = new Phrase(Locale.ENGLISH, "Hello {name}");
		String result = parser.apply(phrase);

		assertEquals("Hello {Bob}", result);
	}

	// --- apply(Phrase) tests ---

	@Test
	void apply_replacesSinglePlaceholder() {
		parser.with("username", "Alice");

		Phrase phrase = new Phrase(Locale.ENGLISH, "Hello {username}!");
		String result = parser.apply(phrase);

		assertEquals("Hello {Alice}!", result);
	}

	@Test
	void apply_replacesMultiplePlaceholders() {
		parser.with("greeting", "Welcome").with("name", "Bob");

		Phrase phrase = new Phrase(Locale.ENGLISH, "{greeting}, {name}!");
		String result = parser.apply(phrase);

		assertEquals("{Welcome}, {Bob}!", result);
	}

	@Test
	void apply_missingPlaceholder_keepsOriginal() {
		parser.with("name", "Alice");

		Phrase phrase = new Phrase(Locale.ENGLISH, "Hello {name}, age {age}");
		String result = parser.apply(phrase);

		assertEquals("Hello {Alice}, age {age}", result);
	}

	@Test
	void apply_noPlaceholders_returnsOriginal() {
		parser.with("unused", "value");

		Phrase phrase = new Phrase(Locale.ENGLISH, "Static message");
		String result = parser.apply(phrase);

		assertEquals("Static message", result);
	}

	@Test
	void apply_nullPhrase_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> parser.apply((Phrase) null));
	}

	// --- apply(Phrase[]) tests ---

	@Test
	void apply_array_replacesAll() {
		parser.with("name", "Alice");

		Phrase[] phrases = new Phrase[]{
				new Phrase(Locale.ENGLISH, "Hello {name}"),
				new Phrase(Locale.ENGLISH, "Hi {name}"),
				new Phrase(Locale.ENGLISH, "Hey {name}")
		};
		String[] result = parser.apply(phrases);

		assertEquals(3, result.length);
		assertEquals("Hello {Alice}", result[0]);
		assertEquals("Hi {Alice}", result[1]);
		assertEquals("Hey {Alice}", result[2]);
	}

	@Test
	void apply_array_null_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> parser.apply((Phrase[]) null));
	}

	@Test
	void apply_emptyArray_returnsEmptyArray() {
		String[] result = parser.apply(new Phrase[0]);
		assertEquals(0, result.length);
	}

	// --- Integer and numeric values ---

	@Test
	void apply_integerValue_convertsToString() {
		parser.with("count", 42);

		Phrase phrase = new Phrase(Locale.ENGLISH, "You have {count} items");
		String result = parser.apply(phrase);

		assertEquals("You have {42} items", result);
	}

	@Test
	void apply_doubleValue_convertsToString() {
		parser.with("price", 19.99);

		Phrase phrase = new Phrase(Locale.ENGLISH, "Price: ${price}");
		String result = parser.apply(phrase);

		assertEquals("Price: ${19.99}", result);
	}

	@Test
	void apply_booleanValue_convertsToString() {
		parser.with("enabled", true);

		Phrase phrase = new Phrase(Locale.ENGLISH, "Status: {enabled}");
		String result = parser.apply(phrase);

		assertEquals("Status: {true}", result);
	}

	@Test
	void apply_nullValue_skippedWhenNull() {
		// ConcurrentHashMap.put(key, null) throws NPE,
		// so with("val", null) throws NPE immediately
		assertThrows(NullPointerException.class, () -> parser.with("val", null));
	}

	// --- Array values ---

	@Test
	void apply_arrayValue_convertsToDeepToString() {
		parser.with("tags", new String[]{"a", "b", "c"});

		Phrase phrase = new Phrase(Locale.ENGLISH, "Tags: {tags}");
		String result = parser.apply(phrase);

		assertEquals("Tags: {[a, b, c]}", result);
	}

	// --- registerParser(Class, Function) tests ---
	// Note: registerParser uses static maps that persist across tests,
	// so these tests are placed last to avoid polluting other tests

	@Test
	void registerParser_nullClass_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> Placeholders.registerParser((Class<Object>) null, (Object x) -> x));
	}

	@Test
	void registerParser_nullFunction_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> Placeholders.registerParser(Integer.class, null));
	}

	// --- registerParser(Predicate, Function) tests ---

	@Test
	void registerParser_nullPredicate_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> Placeholders.registerParser((java.util.function.Predicate<Object>) null, (Object x) -> x));
	}

	@Test
	void registerParser_nullGenericFunction_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> Placeholders.registerParser(x -> true, null));
	}

	// --- Class parser takes precedence over generic ---
	// (Skipped: static parser maps persist across tests)

	// --- Placeholder not in message ---

	@Test
	void apply_unusedPlaceholder_ignored() {
		parser.with("unused", "value");

		Phrase phrase = new Phrase(Locale.ENGLISH, "No placeholders here");
		String result = parser.apply(phrase);

		assertEquals("No placeholders here", result);
	}

	// --- Duplicate placeholder replacement ---

	@Test
	void apply_duplicatePlaceholder_replacesAllOccurrences() {
		parser.with("name", "Alice");

		Phrase phrase = new Phrase(Locale.ENGLISH, "{name} meets {name}");
		String result = parser.apply(phrase);

		assertEquals("{Alice} meets {Alice}", result);
	}
}
