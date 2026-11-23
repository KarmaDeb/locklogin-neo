package com.github.karmadeb.locklogin.neo.api.i18n.bundle;

import com.github.karmadeb.locklogin.neo.api.configuration.ConfigReader;
import com.github.karmadeb.locklogin.neo.api.i18n.phrase.Phrase;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The localization reader is a very similar interface to
 * {@link ConfigReader} with the difference it only supports
 * reading strings
 */
public interface LocaleReader {

	/**
	 * Get the locale
	 * @return the locale
	 */
	Locale locale();

	/**
	 * Get if a phrase is defined
	 * @param key the phrase key
	 * @return if the phrase exists
	 */
	boolean defined(final @NotNull String key);

	/**
	 * Get a phrase, using the provided default phrase
	 * message if none is found
	 * @param key the phrase key
	 * @param def the default phrase
	 * @return the phrase
	 */
	@NotNull
	default Phrase[] get(final @NotNull String key, final @NotNull String def) {
		return this.get(key, new String[]{def});
	}

	/**
	 * Get a collection of phrases, using the provided
	 * default phrase messages if none is found. An empty
	 * collection of messages is also considered valid
	 * @param key the phrase key
	 * @param def the default phrases
	 * @return the phrases
	 */
	default Phrase[] get(final @NotNull String key, final @NotNull String... def) {
		return this.get(key, Arrays.asList(def));
	}

	/**
	 * Get a collection of phrases, using the provided
	 * default phrase messages if none is found. An empty
	 * collection of messages is also considered valid
	 * @param key the phrase key
	 * @param def the default phrases
	 * @return the phrases
	 */
	default Phrase[] get(final @NotNull String key, final @NotNull Collection<String> def) {
		return this.find(key).orElse(def.stream().map(p -> new Phrase(this.locale(), p))
				.toArray(Phrase[]::new));
	}

	/**
	 * Find a phrase. If no phrase is found, this method
	 * returns an empty optional
	 * @param key the phrase key
	 * @return the phrase
	 */
	@NotNull
	Optional<Phrase[]> find(final @NotNull String key);
}
