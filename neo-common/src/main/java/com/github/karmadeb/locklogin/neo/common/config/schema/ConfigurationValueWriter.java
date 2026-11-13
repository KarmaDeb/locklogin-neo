package com.github.karmadeb.locklogin.neo.common.config.schema;

import com.github.karmadeb.locklogin.neo.api.configuration.schema.ValueWriter;
import com.github.karmadeb.locklogin.neo.common.SneakyFunction;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ConfigurationValueWriter<T> implements ValueWriter<T> {

	public enum Type implements Function<String, Object> {
		STRING(s -> s),
		BYTE(Byte::parseByte),
		SHORT(Short::parseShort),
		INT(Integer::parseInt),
		LONG(Long::parseLong),
		FLOAT(Float::parseFloat),
		DOUBLE(Double::parseDouble),
		BOOLEAN(Boolean::parseBoolean),
		MAPPING,
		STRING_LIST,
		MAPPING_LIST;

		private final SneakyFunction<String, Object> transposer;

		Type() {
			this(_ -> { throw new UnsupportedOperationException(); });
		}

		Type(final SneakyFunction<String, Object> transposer) {
			this.transposer = transposer;
		}

		/**
		 * Applies this function to the given argument.
		 *
		 * @param s the function argument
		 * @return the function result
		 */
		@Override
		public Object apply(final String s) {
			try {
				this.transposer.apply(s);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}

			return null;
		}
	}

	private final String key;
	private final String[] comment;

	private final Type type;

	private T constant;
	private Supplier<T> requiredValue;
	private Supplier<T> defaultValue;
	private Predicate<Object> validator;

	public ConfigurationValueWriter(final String key, final String[] comment, final Type type) {
		this.key = key;
		this.comment = comment;
		this.type = type;
	}

	public Type type() {
		return this.type;
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

	public Object resolve(final ScalarNode scalar) {
		String raw = scalar.getValue();
		return this.type.apply(raw);
	}
}
