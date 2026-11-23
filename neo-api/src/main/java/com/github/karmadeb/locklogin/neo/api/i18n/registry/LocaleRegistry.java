package com.github.karmadeb.locklogin.neo.api.i18n.registry;

import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPart;
import com.github.karmadeb.locklogin.neo.api.i18n.Localization;
import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigRegistry;
import com.github.karmadeb.locklogin.neo.api.i18n.bundle.LocaleReader;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * The config registry holds all the configuration
 * files and {@link ConfigPart} registered through
 * extensions or by LockLogin itself. Unlike in
 * {@link ConfigRegistry}, localization does not apply
 * any kind of "merging", meaning different {@link Localization} in
 * the same {@link LocaleDir} overwrite each others
 */
public abstract class LocaleRegistry {

	/**
	 * Append a part to a configuration file
	 * @param path the localizations directory
	 * @param locales the locales
	 */
	public abstract void append(final @NotNull LocaleDir path, final @NotNull Localization locales);

	/**
	 * Obtain a new localization reader. Unlike configuration, localization
	 * is stored as a single unit of work in the whole directory. If a locale
	 * does not exist inside the {@link LocaleDir}, the first available is tried.
	 * @param dir the locale directory
	 * @param locale the locale
	 * @return the locale reader
	 */
	@NotNull
	public abstract LocaleReader bundle(final @NotNull LocaleDir dir, final @NotNull Locale locale);
}
