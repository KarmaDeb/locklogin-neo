package com.github.karmadeb.locklogin.neo.api.configuration;

import com.github.karmadeb.locklogin.neo.api.configuration.schema.SchemaWriter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The configuration writer allows to dynamically write in the
 * configuration file during initialization. Regardless "writer"
 * on its name, it can read existing properties in the configuration
 * file in order to allow some form of validation and updating
 * during initialization.
 * However, for validation and updating is recommended to use the
 * {@link #schema()}
 */
public interface ConfigWriter extends ConfigReader {

	/**
	 * Set the comments for the specified
	 * key in the configuration. If the key does
	 * not exist when the configuration is generated,
	 * the comment is not added.
	 * @param key the key
	 * @param comments the key comments
	 */
	void comment(final @NotNull String key, final @NotNull String... comments);

	/**
	 * Remove a configuration key
	 * @param key the key to remove
	 */
	void erase(final @NotNull String key);

	/**
	 * Write a string in the configuration
	 * @param key the string key
	 * @param value the string value
	 */
	void writeString(final @NotNull String key, final @NotNull String value);

	/**
	 * Write a byte in the configuration
	 * @param key the byte key
	 * @param value the byte value
	 */
	void writeByte(final @NotNull String key, final @NotNull Byte value);

	/**
	 * Write a short in the configuration
	 * @param key the short key
	 * @param value the short value
	 */
	void writeShort(final @NotNull String key, final @NotNull Short value);

	/**
	 * Write an int in the configuration
	 * @param key the int key
	 * @param value the int value
	 */
	void writeInt(final @NotNull String key, final @NotNull Integer value);

	/**
	 * Write a long in the configuration
	 * @param key the long key
	 * @param value the long value
	 */
	void writeLong(final @NotNull String key, final @NotNull Long value);

	/**
	 * Write a float in the configuration
	 * @param key the float key
	 * @param value the float value
	 */
	void writeFloat(final @NotNull String key, final @NotNull Float value);

	/**
	 * Write a double in the configuration
	 * @param key the double key
	 * @param value the double value
	 */
	void writeDouble(final @NotNull String key, final @NotNull Double value);

	/**
	 * Write a boolean in the configuration
	 * @param key the boolean key
	 * @param value the boolean value
	 */
	void writeBool(final @NotNull String key, final @NotNull Boolean value);

	/**
	 * Write a section in the configuration
	 * @param key the section key
	 * @param writer the section writer
	 */
	void writeSection(final @NotNull String key, final @NotNull Consumer<ConfigWriter> writer);

	/**
	 * Write a sections in the configuration
	 * @param key the sections key
	 * @param writer the sections writer
	 */
	void writeSections(final @NotNull String key, final @NotNull Function<Supplier<ConfigWriter>, List<ConfigWriter>> writer);

	/**
	 * Write a string list in the configuration
	 * @param key the string list key
	 * @param value the string list value
	 */
	void writeStringList(final @NotNull String key, final @NotNull List<String> value);

	/**
	 * Write a schema for this configuration writer
	 * @return the schema writer
	 */
	SchemaWriter schema();
}
