package com.github.karmadeb.locklogin.neo.api.configuration.schema;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The configuration schema property writer allows to configure
 * a property value type.
 */
public interface PropertyWriter {

	/**
	 * Write a string
	 * @param configuration the string configuration
	 * @return the schema writer
	 */
	SchemaWriter string(final @NotNull Consumer<ValueWriter<String>> configuration);

	/**
	 * Write a byte
	 * @param configuration the byte configuration
	 * @return the schema writer
	 */
	SchemaWriter byteNumber(final @NotNull Consumer<ValueWriter<Byte>> configuration);

	/**
	 * Write a short
	 * @param configuration the short configuration
	 * @return the schema writer
	 */
	SchemaWriter shortNumber(final @NotNull Consumer<ValueWriter<Short>> configuration);

	/**
	 * Write an int
	 * @param configuration the int configuration
	 * @return the schema writer
	 */
	SchemaWriter intNumber(final @NotNull Consumer<ValueWriter<Integer>> configuration);

	/**
	 * Write a long
	 * @param configuration the long configuration
	 * @return the schema writer
	 */
	SchemaWriter longNumber(final @NotNull Consumer<ValueWriter<Long>> configuration);

	/**
	 * Write a float
	 * @param configuration the float configuration
	 * @return the schema writer
	 */
	SchemaWriter floatNumber(final @NotNull Consumer<ValueWriter<Float>> configuration);

	/**
	 * Write a double
	 * @param configuration the double configuration
	 * @return the schema writer
	 */
	SchemaWriter doubleNumber(final @NotNull Consumer<ValueWriter<Double>> configuration);

	/**
	 * Write a boolean
	 * @param configuration the boolean configuration
	 * @return the schema writer
	 */
	SchemaWriter bool(final @NotNull Consumer<ValueWriter<Boolean>> configuration);

	/**
	 * Write a section
	 * @param configuration the section configuration
	 * @return the schema writer
	 */
	SchemaWriter section(final @NotNull BiConsumer<Supplier<SchemaWriter>, ValueWriter<SchemaWriter>> configuration);

	/**
	 * Write a collection of sections
	 * @param configuration the collection of sections configuration
	 * @return the schema writer
	 */
	SchemaWriter sections(final @NotNull BiConsumer<Supplier<SchemaWriter>, ValueWriter<List<SchemaWriter>>> configuration);

	/**
	 * Write a collection
	 * @param configuration the collection configuration
	 * @return the schema writer
	 */
	SchemaWriter list(final @NotNull Consumer<ValueWriter<List<String>>> configuration);
}
