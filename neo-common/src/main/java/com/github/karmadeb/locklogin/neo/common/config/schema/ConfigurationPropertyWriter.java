package com.github.karmadeb.locklogin.neo.common.config.schema;

import com.github.karmadeb.locklogin.neo.api.configuration.schema.PropertyWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.schema.SchemaWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.schema.ValueWriter;
import com.github.karmadeb.locklogin.neo.common.config.ConfigurationWriter;
import com.github.karmadeb.locklogin.neo.common.config.schema.ConfigurationValueWriter.Type;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigurationPropertyWriter implements PropertyWriter {

	private final ConfigurationSchemaWriter schema;
	private final String key;
	private final String[] comment;

	public ConfigurationPropertyWriter(final ConfigurationSchemaWriter schema,
									   final String key,
									   final String[] comment) {
		this.schema = schema;
		this.key = key;
		this.comment = comment;
	}

	/**
	 * Write a string
	 *
	 * @param configuration the string configuration
	 * @return the schema writer
	 */
	@Override
	public SchemaWriter string(final @NotNull Consumer<ValueWriter<String>> configuration) {
		ConfigurationValueWriter<String> value = new ConfigurationValueWriter<>(this.key, this.comment,
				Type.STRING);
		configuration.accept(value);

		this.schema.values.put(this.key, value);
		return this.schema;
	}

	/**
	 * Write a byte
	 *
	 * @param configuration the byte configuration
	 * @return the schema writer
	 */
	@Override
	public SchemaWriter byteNumber(final @NotNull Consumer<ValueWriter<Byte>> configuration) {
		ConfigurationValueWriter<Byte> value = new ConfigurationValueWriter<>(this.key, this.comment,
				Type.BYTE);
		configuration.accept(value);

		this.schema.values.put(this.key, value);
		return this.schema;
	}

	/**
	 * Write a short
	 *
	 * @param configuration the short configuration
	 * @return the schema writer
	 */
	@Override
	public SchemaWriter shortNumber(final @NotNull Consumer<ValueWriter<Short>> configuration) {
		ConfigurationValueWriter<Short> value = new ConfigurationValueWriter<>(this.key, this.comment,
				Type.SHORT);
		configuration.accept(value);

		this.schema.values.put(this.key, value);
		return this.schema;
	}

	/**
	 * Write an int
	 *
	 * @param configuration the int configuration
	 * @return the schema writer
	 */
	@Override
	public SchemaWriter intNumber(final @NotNull Consumer<ValueWriter<Integer>> configuration) {
		ConfigurationValueWriter<Integer> value = new ConfigurationValueWriter<>(this.key, this.comment,
				Type.INT);
		configuration.accept(value);

		this.schema.values.put(this.key, value);
		return this.schema;
	}

	/**
	 * Write a long
	 *
	 * @param configuration the long configuration
	 * @return the schema writer
	 */
	@Override
	public SchemaWriter longNumber(final @NotNull Consumer<ValueWriter<Long>> configuration) {
		ConfigurationValueWriter<Long> value = new ConfigurationValueWriter<>(this.key, this.comment,
				Type.LONG);
		configuration.accept(value);

		this.schema.values.put(this.key, value);
		return this.schema;
	}

	/**
	 * Write a float
	 *
	 * @param configuration the float configuration
	 * @return the schema writer
	 */
	@Override
	public SchemaWriter floatNumber(final @NotNull Consumer<ValueWriter<Float>> configuration) {
		ConfigurationValueWriter<Float> value = new ConfigurationValueWriter<>(this.key, this.comment,
				Type.FLOAT);
		configuration.accept(value);

		this.schema.values.put(this.key, value);
		return this.schema;
	}

	/**
	 * Write a double
	 *
	 * @param configuration the double configuration
	 * @return the schema writer
	 */
	@Override
	public SchemaWriter doubleNumber(final @NotNull Consumer<ValueWriter<Double>> configuration) {
		ConfigurationValueWriter<Double> value = new ConfigurationValueWriter<>(this.key, this.comment,
				Type.DOUBLE);
		configuration.accept(value);

		this.schema.values.put(this.key, value);
		return this.schema;
	}

	/**
	 * Write a boolean
	 *
	 * @param configuration the boolean configuration
	 * @return the schema writer
	 */
	@Override
	public SchemaWriter bool(final @NotNull Consumer<ValueWriter<Boolean>> configuration) {
		ConfigurationValueWriter<Boolean> value = new ConfigurationValueWriter<>(this.key, this.comment,
				Type.BOOLEAN);
		configuration.accept(value);

		this.schema.values.put(this.key, value);
		return this.schema;
	}

	/**
	 * Write a section
	 *
	 * @param configuration the section configuration
	 * @return the schema writer
	 */
	@Override
	public SchemaWriter section(final @NotNull BiConsumer<Supplier<SchemaWriter>, ValueWriter<SchemaWriter>> configuration) {
		ConfigurationValueWriter<SchemaWriter> value = new ConfigurationValueWriter<>(this.key, this.comment,
				Type.MAPPING);
		configuration.accept(ConfigurationPropertyWriter::makeSchema, value);

		this.schema.values.put(this.key, value);
		return this.schema;
	}

	/**
	 * Write a collection of sections
	 *
	 * @param configuration the collection of sections configuration
	 * @return the schema writer
	 */
	@Override
	public SchemaWriter sections(final @NotNull BiConsumer<Supplier<SchemaWriter>, ValueWriter<List<SchemaWriter>>> configuration) {
		ConfigurationValueWriter<List<SchemaWriter>> value = new ConfigurationValueWriter<>(this.key, this.comment,
				Type.MAPPING_LIST);
		configuration.accept(ConfigurationPropertyWriter::makeSchema, value);

		this.schema.values.put(this.key, value);
		return this.schema;
	}

	/**
	 * Write a collection
	 *
	 * @param configuration the collection configuration
	 * @return the schema writer
	 */
	@Override
	public SchemaWriter list(final @NotNull Consumer<ValueWriter<List<String>>> configuration) {
		ConfigurationValueWriter<List<String>> value = new ConfigurationValueWriter<>(this.key, this.comment,
				Type.STRING_LIST);
		configuration.accept(value);

		this.schema.values.put(this.key, value);
		return this.schema;
	}

	private static SchemaWriter makeSchema() {
		MappingNode section = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
		ConfigurationWriter writer = new ConfigurationWriter(section);

		return new ConfigurationSchemaWriter(writer);
	}
}
