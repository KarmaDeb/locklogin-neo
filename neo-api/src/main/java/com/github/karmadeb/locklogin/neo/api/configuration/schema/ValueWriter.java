package com.github.karmadeb.locklogin.neo.api.configuration.schema;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.Supplier;


/**
 * The configuration schema value writer allows to configure the
 * value of a property.
 * @param <T> the value type
 */
public interface ValueWriter<T> {

	/**
	 * Define the value as a constant value. This value cannot
	 * be changed and any change in the physic file is reverted
	 * on each initialization
	 * @param value the constant value
	 * @return the value writer
	 */
	ValueWriter<T> constant(final @NotNull T value);

	/**
	 * Define the value as "required" (meaning no invalid value
	 * can be provided). Marking a value as required, requires also
	 * a default value
	 * @param defaultValue the default value
	 * @return the value writer
	 */
	default ValueWriter<T> required(final @NotNull T defaultValue) {
		return required(() -> defaultValue);
	}

	/**
	 * Define the value as "required" (meaning no invalid value
	 * can be provided). Marking a value as required, requires also
	 * a default value
	 * @param defaultValue the default value provider
	 * @return the value writer
	 */
	ValueWriter<T> required(final @NotNull Supplier<T> defaultValue);

	/**
	 * Define the initial value of the property. This does not imply any
	 * kind of validation (unlike {@link #required(Object)}
	 * @param defaultValue the default value
	 * @return the value writer
	 */
	default ValueWriter<T> with(final @NotNull T defaultValue) {
		return with(() -> defaultValue);
	}

	/**
	 * Define the initial value of the property. This does not imply any
	 * kind of validation (unlike {@link #required(Object)}
	 * @param defaultValue the default value provider
	 * @return the value writer
	 */
	ValueWriter<T> with(final @NotNull Supplier<T> defaultValue);

	/**
	 * Register a value validator. This can also be used as an
	 * "updated" in order to allow changes of property value types
	 * between versions. If the value of the predicate is false, the
	 * value is considered not valid and the default value (requires {@link #required(Object)})
	 * is used. If no default value is provided, the property is removed
	 * from the configuration file
	 * @param value the value validator
	 * @return the value writer
	 */
	ValueWriter<T> validator(final @NotNull Predicate<Object> value);
}
