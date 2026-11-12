package com.github.karmadeb.locklogin.neo.api.configuration.schema;

import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import org.jetbrains.annotations.NotNull;

/**
 * The configuration schema writer allows to create a schema
 * on runtime for extending configuration in an easier way other
 * than manually using {@link com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter}
 */
public interface SchemaWriter {

	/**
	 * Start a new key in the schema
	 * @param key the schema key
	 * @param comments the property comments
	 * @return the schema property
	 */
	PropertyWriter with(final @NotNull String key, final @NotNull String... comments);

	/**
	 * Build the schema
	 * @return the configuration writer
	 */
	ConfigWriter build();
}