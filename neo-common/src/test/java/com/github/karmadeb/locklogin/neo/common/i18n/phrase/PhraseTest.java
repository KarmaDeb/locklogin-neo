package com.github.karmadeb.locklogin.neo.common.i18n.phrase;

import com.github.karmadeb.locklogin.neo.api.i18n.phrase.Phrase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PhraseTest {

	// --- Constructor tests ---

	@Test
	void constructor_setsLocale() {
		Phrase phrase = new Phrase(Locale.FRENCH, "Bonjour");

		assertEquals(Locale.FRENCH, phrase.locale());
	}

	@Test
	void constructor_setsMessage() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "Hello world");

		assertEquals("Hello world", phrase.message());
	}

	@Test
	void constructor_nullLocale_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> new Phrase(null, "message"));
	}

	@Test
	void constructor_nullMessage_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> new Phrase(Locale.ENGLISH, null));
	}

	// --- message() tests ---

	@Test
	void message_returnsOriginalMessage() {
		String message = "Hello, {username}!";
		Phrase phrase = new Phrase(Locale.ENGLISH, message);

		assertEquals(message, phrase.message());
	}

	@Test
	void message_returnsEmptyString() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "");

		assertEquals("", phrase.message());
	}

	// --- placeholders() tests ---

	@Test
	void placeholders_noPlaceholders_returnsEmptySet() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "Hello world");

		assertTrue(phrase.placeholders().isEmpty());
	}

	@Test
	void placeholders_singlePlaceholder_returnsOne() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "Hello {name}");

		assertEquals(Set.of("name"), phrase.placeholders());
	}

	@Test
	void placeholders_multiplePlaceholders_returnsAll() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "{greeting}, {name}!");

		Set<String> expected = new java.util.LinkedHashSet<>();
		expected.add("greeting");
		expected.add("name");
		assertEquals(expected, phrase.placeholders());
	}

	@Test
	void placeholders_duplicatePlaceholder_returnsOnce() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "{name} said hello to {name}");

		assertEquals(Set.of("name"), phrase.placeholders());
	}

	@Test
	void placeholders_escapedBrace_notDetected() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "\\{name}");

		assertTrue(phrase.placeholders().isEmpty());
	}

	@Test
	void placeholders_doubleBrace_notDetected() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "{{name}}");

		assertTrue(phrase.placeholders().isEmpty());
	}

	@Test
	void placeholders_tripleBrace_detected() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "{{{name}}}");

		assertEquals(Set.of("name"), phrase.placeholders());
	}

	@Test
	void placeholders_mixedContent_returnsPlaceholders() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "You have {count} new {type} messages");

		Set<String> expected = new java.util.LinkedHashSet<>();
		expected.add("count");
		expected.add("type");
		assertEquals(expected, phrase.placeholders());
	}

	@Test
	void placeholders_specialCharactersInKey_detected() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "Value: {user_name-1}");

		assertEquals(Set.of("user_name-1"), phrase.placeholders());
	}

	@Test
	void placeholders_emptyBraces_detected() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "Empty: {}");

		assertEquals(Set.of(""), phrase.placeholders());
	}

	@Test
	void placeholders_unmatchedBrace_notDetected() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "Open { brace");

		assertTrue(phrase.placeholders().isEmpty());
	}

	@Test
	void placeholders_returnsUnmodifiableSet() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "{key}");

		assertThrows(UnsupportedOperationException.class, () -> phrase.placeholders().add("other"));
	}

	@Test
	void placeholders_preservesOrder() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "{first} then {second} then {third}");

		// SequencedSet preserves insertion order
		List<String> list = new java.util.ArrayList<>(phrase.placeholders());
		assertEquals(3, list.size());
		assertEquals("first", list.get(0));
		assertEquals("second", list.get(1));
		assertEquals("third", list.get(2));
	}

	@Test
	void placeholders_multipleEscaped_notDetected() {
		Phrase phrase = new Phrase(Locale.ENGLISH, "\\\\{name}");

		// Two backslashes = escaped backslash, so {name} is a real placeholder
		assertEquals(Set.of("name"), phrase.placeholders());
	}


	// --- Locale variations ---

	@Test
	void constructor_withComplexLocale() {
		Locale locale = Locale.of("en", "US", "Variant");
		Phrase phrase = new Phrase(locale, "Hello");

		assertEquals(locale, phrase.locale());
	}

	@Test
	void constructor_withCountryLocale() {
		Phrase phrase = new Phrase(Locale.of("fr", "CA"), "Bonjour");

		assertEquals(Locale.of("fr", "CA"), phrase.locale());
	}
}
