package com.github.karmadeb.locklogin.neo.api.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * The configuration reader allows to read from the configuration after
 * initialization.
 */
public interface ConfigReader {

	/**
	 * Get if key exists in the configuration
	 * file
	 * @param key the key
	 * @return if the key exists
	 */
	boolean has(final String key);

	/**
	 * Get a string value
	 * @param key the string key
	 * @return the string value
	 */
	Optional<String> getString(final @NotNull String key);

	/**
	 * Get a byte value
	 * @param key the byte key
	 * @return the byte value
	 */
	Optional<Byte> getByte(final @NotNull String key);

	/**
	 * Get a short value
	 * @param key the short key
	 * @return the short value
	 */
	Optional<Short> getShort(final @NotNull String key);

	/**
	 * Get an int value
	 * @param key the int key
	 * @return the int value
	 */
	Optional<Integer> getInt(final @NotNull String key);

	/**
	 * Get a long value
	 * @param key the long key
	 * @return the long value
	 */
	Optional<Long> getLong(final @NotNull String key);

	/**
	 * Get a float value
	 * @param key the float key
	 * @return the float value
	 */
	Optional<Float> getFloat(final @NotNull String key);

	/**
	 * Get a double value
	 * @param key the double key
	 * @return the double value
	 */
	Optional<Double> getDouble(final @NotNull String key);

	/**
	 * Get a boolean value
	 * @param key the boolean key
	 * @return the boolean value
	 */
	Optional<Boolean> getBool(final @NotNull String key);

	/**
	 * Get a section value
	 * @param key the section key
	 * @return the section value
	 */
	Optional<? extends ConfigReader> getSection(final @NotNull String key);

	/**
	 * Get a sections value
	 * @param key the sections key
	 * @return the sections value
	 */
	Optional<? extends List<? extends ConfigReader>> getSections(final @NotNull String key);

	/**
	 * Get a string list value
	 * @param key the string list key
	 * @return the string list value
	 */
	List<String> getStringList(final @NotNull String key);
}
