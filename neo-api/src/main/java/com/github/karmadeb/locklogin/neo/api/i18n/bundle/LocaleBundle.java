package com.github.karmadeb.locklogin.neo.api.i18n.bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Locale;

/// The localization bundle allows to define multi-language localizations
/// from a single entry-point. Implementations should update {@link #locales()}
/// as calls to {@link #reader(Locale)} or {@link #writer(Locale)} are made for
/// locales not present in the collection. Additionally, creating or writing new
/// locales should also write them in the other locales. For instance, doing:
/// ```java
/// LocalizationWriter english = bundle.writer(Locale.ENGLISH);
/// LocalizationWriter spanish = bundle.writer(Locale.SPANISH);
///
/// english.write("key", "Hello world");
/// spanish.write("key2", "¿Cómo estás?");
/// spanish.write("key3", "Mi nombre es {username}");
/// english.write("key3", "My name is {username}");
/// ```
/// Should produce:
/// ```yaml
/// # i18n/localization.en.yml
/// key: "Hello world"
/// key2: "¿Cómo estás?"
/// key3: "My name is {username}"
/// ```
/// And
/// ```yaml
/// # i18n/localization.es.yml
/// key: "Hello world"
/// key2: "¿Cómo estás?"
/// key3: "Mi nombre es {username}"
/// ```
///
/// Notice how Spanish did not overwrite "key" and got the one defined in the first locale who defined it, while
/// English did overwrite "key3" from Spanish. Writing a key in a single localization writer should write the same
/// key in all the others if not defined
public interface LocaleBundle {

	/**
	 * Get the bundle supported locales
	 *
	 * @return the locales
	 */
	@NotNull
	@Unmodifiable
	Collection<Locale> locales();

	/**
	 * Get the bundle reader
	 *
	 * @param locale the locale
	 * @return the reader
	 */
	@NotNull
	LocaleReader reader(final @NotNull Locale locale);

	/**
	 * Get the bundle writer
	 *
	 * @param locale the locale
	 * @return the writer
	 */
	@NotNull
	LocaleWriter writer(final @NotNull Locale locale);
}
