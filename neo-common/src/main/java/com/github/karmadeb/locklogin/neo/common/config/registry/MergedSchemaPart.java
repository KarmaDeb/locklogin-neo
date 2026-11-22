package com.github.karmadeb.locklogin.neo.common.config.registry;

import com.github.karmadeb.locklogin.neo.api.configuration.ConfigReader;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPart;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPath;
import com.github.karmadeb.locklogin.neo.common.config.ConfigurationWriter;
import com.github.karmadeb.locklogin.neo.common.config.schema.ConfigurationSchemaWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.*;

public final class MergedSchemaPart extends ConfigPart {

	private final Set<ConfigPart> parts = new LinkedHashSet<>();

	private final ConfigPath path;

	@Contract("null -> fail")
	public MergedSchemaPart(final ConfigPath path) {
		this.path = Objects.requireNonNull(path);
	}

	@Contract("null -> fail")
	public void add(final ConfigPart part) {
		Objects.requireNonNull(part);
		parts.add(part);
	}

	@Contract("null -> fail")
	public void remove(final ConfigPart part) {
		Objects.requireNonNull(part);
		parts.remove(part);
	}

	@Contract("null -> fail")
	public <T extends ConfigPart> Optional<T> find(final Class<T> type) {
		Objects.requireNonNull(type);
		return this.parts.stream()
				.filter(type::isInstance)
				.map(type::cast)
				.findAny();
	}

	/**
	 * Get the part path. This is used internally
	 * when generating the {@link ConfigWriter}. Consider
	 * it the "scope". Using {@link ConfigPath#empty()} will write
	 * in the ROOT of the document, and using an existing
	 * section key will allow to overwrite its contents
	 *
	 * @return the config part
	 */
	@Override
	public @NotNull ConfigPath path() {
		return this.path;
	}

	/**
	 * Initialize the configuration
	 *
	 * @param reader the reader
	 * @param writer the writer
	 */
	@Override
	public void init(final @NotNull ConfigReader reader, final @NotNull ConfigWriter writer) {
		ConfigurationWriter configWriter = (ConfigurationWriter) writer;
		ConfigurationSchemaWriter mergedSchema = null;

		for (ConfigPart part : this.parts) {
			ConfigurationWriter tempWriter = new ConfigurationWriter(
					new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO));
			part.init(reader, tempWriter);
			ConfigurationSchemaWriter schema = tempWriter.schema();

			if (mergedSchema == null) {
				mergedSchema = schema;
			} else {
				mergedSchema = (ConfigurationSchemaWriter) mergedSchema.merge(schema);
			}
		}

		if (mergedSchema != null) {
			ConfigurationSchemaWriter targetSchema = configWriter.schema();
			ConfigurationSchemaWriter combined = (ConfigurationSchemaWriter) targetSchema.merge(mergedSchema);

			targetSchema.values.clear();
			targetSchema.values.putAll(combined.values);
		}
	}
}
