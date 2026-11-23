package com.github.karmadeb.locklogin.neo.api.i18n.phrase;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * The localization represents a localization message.
 */
public final class Phrase {

	private final Locale locale;
	private final String message;
	private final SequencedSet<String> placeholders;

	/**
	 * Create a new localization message
	 * @param locale the message locale
	 * @param message the message
	 */
	@Contract("null, _ -> fail; _, null -> fail")
	public Phrase(Locale locale, String message) {
		this.locale = Objects.requireNonNull(locale);
		this.message = Objects.requireNonNull(message);

		KeyScanner scanner = new KeyScanner(this.message);
		this.placeholders = Collections.unmodifiableSequencedSet(scanner.scan());
	}

	/**
	 * Get the locale
	 * @return the locale
	 */
	@NotNull
	public Locale locale() {
		return this.locale;
	}

	/**
	 * Get the message
	 * @return the message
	 */
	@NotNull
	public String message() {
		return this.message;
	}

	/**
	 * Get the placeholders in the message.
	 * This is used by the localization generator to
	 * regenerate the message if needed
	 * @return the placeholders
	 */
	@NotNull @Unmodifiable
	public SequencedSet<String> placeholders() {
		return this.placeholders;
	}

	private record KeyScanner(char[] input) {

		public KeyScanner(final String input) {
			this(input.toCharArray());
		}

		public SequencedSet<String> scan() {
			SequencedSet<String> placeholders = new LinkedHashSet<>();
			int i = 0;

			while (i < this.input.length) {
				if (this.input[i] == '{') {
					int backslashCount = 0;
					int pos = i - 1;
					while (pos >= 0 && this.input[pos] == '\\') {
						backslashCount++;
						pos--;
					}

					if (backslashCount % 2 == 1) {
						i++;
						continue;
					}

					int openBraceCount = 0;
					while (i < this.input.length && this.input[i] == '{') {
						openBraceCount++;
						i++;
					}

					if (openBraceCount % 2 == 0) {
						continue;
					}

					int keyStart = i;
					while (i < this.input.length) {
						if (this.input[i] == '}') {
							int closeBraceCount = 0;
							while (i < this.input.length && this.input[i] == '}') {
								closeBraceCount++;
								i++;
							}

							if (closeBraceCount == openBraceCount) {
								String key = new String(this.input, keyStart, i - openBraceCount - keyStart);
								placeholders.add(key);
							}
							break;
						} else if (this.input[i] == '{') {
							break;
						}

						i++;
					}
				} else {
					i++;
				}
			}

			return placeholders;
		}
	}
}