package com.github.karmadeb.locklogin.neo.common.config.schema;

import com.github.karmadeb.locklogin.neo.api.configuration.schema.ValueWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ConfigurationValueWriter<T> implements ValueWriter<T> {

	private final String key;
	private final String[] comment;

	private T constant;
	private Supplier<T> requiredValue;
	private Supplier<T> defaultValue;
	private Predicate<Object> validator;

	public ConfigurationValueWriter(final String key, final String[] comment) {
		this.key = key;
		this.comment = comment;
	}

	public String key() {
		return this.key;
	}

	public String[] comment() {
		return this.comment;
	}

	public Optional<T> constant() {
		return Optional.ofNullable(this.constant);
	}

	public Optional<Supplier<T>> required() {
		return Optional.ofNullable(this.requiredValue);
	}

	public Optional<Supplier<T>> with() {
		return Optional.ofNullable(this.defaultValue);
	}

	public Optional<Predicate<Object>> validator() {
		return Optional.ofNullable(this.validator);
	}

	/**
	 * Define the value as a constant value. This value cannot
	 * be changed and any change in the physic file is reverted
	 * on each initialization
	 *
	 * @param value the constant value
	 * @return the value writer
	 */
	@Override
	public ConfigurationValueWriter<T> constant(final @NotNull T value) {
		this.constant = value;
		this.requiredValue = () -> value;
		this.defaultValue = () -> value;
		this.validator = _ -> false;

		return this;
	}

	/**
	 * Define the value as "required" (meaning no invalid value
	 * can be provided). Marking a value as required, requires also
	 * a default value
	 *
	 * @param defaultValue the default value provider
	 * @return the value writer
	 */
	@Override
	public ConfigurationValueWriter<T> required(final @NotNull Supplier<T> defaultValue) {
		this.constant = null;

		this.requiredValue = defaultValue;
		if (this.defaultValue != null) {
			this.defaultValue = defaultValue;
		}

		return this;
	}

	/**
	 * Define the initial value of the property. This does not imply any
	 * kind of validation (unlike {@link #required(Object)}
	 *
	 * @param defaultValue the default value provider
	 * @return the value writer
	 */
	@Override
	public ConfigurationValueWriter<T> with(@NotNull Supplier<T> defaultValue) {
		this.constant = null;
		this.defaultValue = defaultValue;

		return this;
	}

	/**
	 * Register a value validator. This can also be used as an
	 * "updated" in order to allow changes of property value types
	 * between versions. If the value of the predicate is false, the
	 * value is considered not valid and the default value (requires {@link #required(Object)})
	 * is used. If no default value is provided, the property is removed
	 * from the configuration file
	 *
	 * @param value the value validator
	 * @return the value writer
	 */
	@Override
	public ConfigurationValueWriter<T> validator(final @NotNull Predicate<Object> value) {
		this.validator = value;
		return this;
	}
}
