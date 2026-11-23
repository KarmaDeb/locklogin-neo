package com.github.karmadeb.locklogin.neo.api.localization;

import com.github.karmadeb.locklogin.neo.api.i18n.phrase.Phrase;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalizationTest {

	@Test
	void basicPlaceholder() {
		Phrase loc = new Phrase(Locale.ENGLISH, "Hello {name}!");
		assertEquals(Set.of("name"), loc.placeholders());
	}

	@Test
	void noPlaceholder() {
		Phrase loc = new Phrase(Locale.ENGLISH, "Hello world!");
		assertEquals(Set.of(), loc.placeholders());
	}

	@Test
	void evenBraceWrappingNotPlaceholder() {
		// {{hello}} displays as {hello} — not a placeholder
		Phrase loc = new Phrase(Locale.ENGLISH, "Say {{hello}}!");
		assertEquals(Set.of(), loc.placeholders());
	}

	@Test
	void oddBraceWrappingIsPlaceholder() {
		// {{{hello}}} is a placeholder with key "hello"
		Phrase loc = new Phrase(Locale.ENGLISH, "Say {{{hello}}}!");
		assertEquals(Set.of("hello"), loc.placeholders());
	}

	@Test
	void quadrupleBraceWrappingNotPlaceholder() {
		// {{{{hello}}}} displays as {{hello}} — not a placeholder
		Phrase loc = new Phrase(Locale.ENGLISH, "Say {{{{hello}}}}!");
		assertEquals(Set.of(), loc.placeholders());
	}

	@Test
	void quintupleBraceWrappingIsPlaceholder() {
		// {{{{{hello}}}}} is a placeholder with key "hello"
		Phrase loc = new Phrase(Locale.ENGLISH, "Say {{{{{hello}}}}}!");
		assertEquals(Set.of("hello"), loc.placeholders());
	}

	@Test
	void escapedBraceNotPlaceholder() {
		// \{hello} — the brace is escaped, not a placeholder
		Phrase loc = new Phrase(Locale.ENGLISH, "Say \\{hello}!");
		assertEquals(Set.of(), loc.placeholders());
	}

	@Test
	void doubleEscapedBraceIsPlaceholder() {
		// \\{hello} — backslash is escaped, so {hello} is a placeholder
		Phrase loc = new Phrase(Locale.ENGLISH, "Say \\\\{hello}!");
		assertEquals(Set.of("hello"), loc.placeholders());
	}

	@Test
	void tripleEscapedBraceNotPlaceholder() {
		// \\\{hello} — two backslashes cancel, leaving \{ which escapes the brace
		Phrase loc = new Phrase(Locale.ENGLISH, "Say \\\\\\{hello}!");
		assertEquals(Set.of(), loc.placeholders());
	}

	@Test
	void multiplePlaceholders() {
		Phrase loc = new Phrase(Locale.ENGLISH, "{greeting} {name}!");
		assertEquals(Set.of("greeting", "name"), loc.placeholders());
	}

	@Test
	void mixedPlaceholdersAndEscaped() {
		// {name} is a placeholder, \{not} is not
		Phrase loc = new Phrase(Locale.ENGLISH, "Hi {name}, not \\{not}!");
		assertEquals(Set.of("name"), loc.placeholders());
	}

	@Test
	void placeholderWithOddWrapsAndText() {
		// {{{key}}} is a placeholder, {{literal}} is not
		Phrase loc = new Phrase(Locale.ENGLISH, "{{literal}} and {{{key}}}");
		assertEquals(Set.of("key"), loc.placeholders());
	}

	@Test
	void emptyPlaceholder() {
		Phrase loc = new Phrase(Locale.ENGLISH, "{}");
		assertEquals(Set.of(""), loc.placeholders());
	}

	@Test
	void unclosedPlaceholder() {
		// {name without closing brace — not a valid placeholder
		Phrase loc = new Phrase(Locale.ENGLISH, "Hi {name");
		assertEquals(Set.of(), loc.placeholders());
	}

	@Test
	void mismatchedBraceCount() {
		// {{name}} with single closing — mismatch, not a placeholder
		Phrase loc = new Phrase(Locale.ENGLISH, "{{name}");
		assertEquals(Set.of(), loc.placeholders());
	}

	@Test
	void duplicatePlaceholders() {
		// {name} appears twice — should only appear once in the set
		Phrase loc = new Phrase(Locale.ENGLISH, "{name} and {name}");
		assertEquals(Set.of("name"), loc.placeholders());
	}
}
