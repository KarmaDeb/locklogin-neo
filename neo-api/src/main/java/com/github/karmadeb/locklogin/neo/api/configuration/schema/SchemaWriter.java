package com.github.karmadeb.locklogin.neo.api.configuration.schema;

import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;

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

	/**
	 * Merge the current schema definition with the
	 * other. This method returns a modified schema writer
	 * with a combination of this schema writer and the other
	 * schema writer. If incompatible types are found,
	 * the other schema is preserved. Alternatively, if
	 * compatible types are found (mapping & mapping list) or
	 * (string & string list) or (mapping list & mapping list) or
	 * (string list & string list), its generators are also merged
	 * to create:
	 * (mapping + mapping list = mapping list&mapping)
	 * (string + string list = string list&string)
	 * (mapping list + mapping list = mapping list1&mapping list2)
	 * (string list + string list = string list1&string list2)
	 * @param other the other schema
	 * @return the modified schema
	 */
	@NotNull
	SchemaWriter merge(final @NotNull SchemaWriter other);

	/**
	 * Get the current schema values. The entire map set is
	 * a snapshot of the current values, meaning modifications
	 * won't and should not affect the schema
	 * @return the schema values
	 */
	@NotNull @Unmodifiable
	Map<String, ? extends @UnmodifiableView ValueWriter<?>> values();
}