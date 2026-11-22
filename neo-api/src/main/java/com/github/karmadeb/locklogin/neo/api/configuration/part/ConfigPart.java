package com.github.karmadeb.locklogin.neo.api.configuration.part;

import com.github.karmadeb.locklogin.neo.api.configuration.ConfigReader;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a part in the configuration file. This can be used
 * by extensions to register their own configuration "sections"
 */
public abstract class ConfigPart {

	/**
	 * Get the part path. This is used internally
	 * when generating the {@link ConfigWriter}. Consider
	 * it the "scope". Using {@link ConfigPath#empty()} will write
	 * in the ROOT of the document, and using an existing
	 * section key will allow to overwrite its contents
	 * @return the config part
	 */
	public abstract @NotNull ConfigPath path();

	/**
	 * Initialize the configuration
	 * @param reader the reader
	 * @param writer the writer
	 */
	public abstract void init(final @NotNull ConfigReader reader, final @NotNull ConfigWriter writer);
}
