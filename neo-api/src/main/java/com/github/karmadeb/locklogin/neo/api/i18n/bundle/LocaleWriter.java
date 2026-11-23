package com.github.karmadeb.locklogin.neo.api.i18n.bundle;

import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * The localization writer is a very similar interface to
 * {@link ConfigWriter} with the difference it only supports
 * writing and reading strings
 */
public interface LocaleWriter extends LocaleReader {

	/**
	 * Remove a localization
	 * @param key the localization key
	 */
	void unset(final @NotNull String key);

	/**
	 * Write a localization. If the key already exists the
	 * localization is not added
	 * @param key the localization key
	 * @param phrase the localization value
	 */
	void write(final @NotNull String key, final @NotNull String phrase);

	/**
	 * Write a localization list
	 * @param key the localization key
	 * @param phrases the localization values
	 */
	void write(final @NotNull String key, final @NotNull Collection<String> phrases);
}