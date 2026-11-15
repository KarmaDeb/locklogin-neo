package com.github.karmadeb.locklogin.neo.api.configuration.schema;

import com.github.karmadeb.locklogin.neo.api.SneakyFunction;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


/**
 * The configuration schema value writer allows to configure the
 * value of a property.
 * @param <T> the value type
 */
public interface ValueWriter<T> {

	/**
	 * Value type
	 */
	enum Type implements Function<String, Object>, Predicate<Object> {
		STRING(s -> s, obj -> obj instanceof String),
		BYTE(Byte::parseByte, obj -> obj instanceof Byte),
		SHORT(Short::parseShort, obj -> obj instanceof Short),
		INT(Integer::parseInt, obj -> obj instanceof Integer),
		LONG(Long::parseLong, obj -> obj instanceof Long),
		FLOAT(Float::parseFloat, obj -> obj instanceof Float),
		DOUBLE(Double::parseDouble, obj -> obj instanceof Double),
		BOOLEAN(Boolean::parseBoolean, obj -> obj instanceof Boolean),
		MAPPING(obj -> obj instanceof ConfigWriter),
		STRING_LIST(obj -> obj instanceof List<?> l && (l.isEmpty() || l.getFirst() instanceof String)),
		MAPPING_LIST(obj -> obj instanceof List<?> l && (l.isEmpty() || l.getFirst() instanceof ConfigWriter));

		private final SneakyFunction<String, Object> transposer;
		private final Predicate<Object> typeCheck;

		Type(final Predicate<Object> typeCheck) {
			this(_ -> { throw new UnsupportedOperationException(); }, typeCheck);
		}

		Type(final SneakyFunction<String, Object> transposer, final Predicate<Object> typeCheck) {
			this.transposer = transposer;
			this.typeCheck = typeCheck;
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
				return this.transposer.apply(s);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		/**
		 * Evaluates this predicate on the given argument.
		 *
		 * @param o the input argument
		 * @return {@code true} if the input argument matches the predicate,
		 * otherwise {@code false}
		 */
		@Override
		public boolean test(final Object o) {
			return this.typeCheck.test(o);
		}
	}

	/**
	 * Get the value type
	 * @return the type of the value
	 */
	@NotNull
	Type type();

	/**
	 * Get the value key
	 * @return the key
	 */
	@NotNull
	String key();

	/**
	 * Get the value comment
	 * @return the comment
	 */
	String @NotNull[] comment();

	/**
	 * Define the value as a constant value. This value cannot
	 * be changed and any change in the physic file is reverted
	 * on each initialization
	 * @param value the constant value
	 * @return the value writer
	 */
	@NotNull
	ValueWriter<T> constant(final @NotNull T value);

	/**
	 * Get the value constant
	 * @return the constant
	 */
	@NotNull
	Optional<T> constant();

	/**
	 * Define the value as "required" (meaning no invalid value
	 * can be provided). Marking a value as required, requires also
	 * a default value
	 * @param defaultValue the default value
	 * @return the value writer
	 */
	@NotNull
	default ValueWriter<T> required(final @NotNull T defaultValue) {
		return required(() -> defaultValue);
	}

	/**
	 * Define the value as "required" (meaning no invalid value
	 * can be provided). Marking a value as required, requires also
	 * a default value. If no validator is provided, the value writer
	 * itself will check automatically if the value type is the same
	 * as the value requires
	 * @param defaultValue the default value provider
	 * @return the value writer
	 */
	@NotNull
	ValueWriter<T> required(final @NotNull Supplier<T> defaultValue);

	/**
	 * Get the value required value provider
	 * @return the required value
	 */
	@NotNull
	Optional<Supplier<T>> required();

	/**
	 * Define the initial value of the property. This does not imply any
	 * kind of validation (unlike {@link #required(Object)}
	 * @param defaultValue the default value
	 * @return the value writer
	 */
	@NotNull
	default ValueWriter<T> with(final @NotNull T defaultValue) {
		return with(() -> defaultValue);
	}

	/**
	 * Define the initial value of the property. This does not imply any
	 * kind of validation (unlike {@link #required(Object)}
	 * @param defaultValue the default value provider
	 * @return the value writer
	 */
	@NotNull
	ValueWriter<T> with(final @NotNull Supplier<T> defaultValue);

	/**
	 * Get the value default value provider
	 * @return the default value
	 */
	@NotNull
	Optional<Supplier<T>> with();

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
	@NotNull
	ValueWriter<T> validator(final @NotNull Predicate<Object> value);

	/**
	 * Get the value validator
	 * @return the validator
	 */
	@NotNull
	Optional<Predicate<Object>> validator();
}
