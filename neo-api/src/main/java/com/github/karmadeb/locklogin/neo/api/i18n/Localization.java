package com.github.karmadeb.locklogin.neo.api.i18n;

import com.github.karmadeb.locklogin.neo.api.i18n.bundle.LocaleBundle;
import org.jetbrains.annotations.NotNull;

/**
 * The localization class allows to initialize multi-language
 * localization files from a single entrypoint
 */
public abstract class Localization {

	/**
	 * Initialize the localization
	 */
	public abstract void init(final @NotNull LocaleBundle bundle);
}
