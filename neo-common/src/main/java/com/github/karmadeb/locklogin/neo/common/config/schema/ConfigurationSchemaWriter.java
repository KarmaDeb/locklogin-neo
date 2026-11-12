package com.github.karmadeb.locklogin.neo.common.config.schema;

import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.schema.PropertyWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.schema.SchemaWriter;
import com.github.karmadeb.locklogin.neo.common.config.ConfigurationWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConfigurationSchemaWriter implements SchemaWriter {

	private final ConfigurationWriter writer;
	final Map<String, ConfigurationValueWriter<?>> values = new HashMap<>();

	@Contract("null -> fail")
	public ConfigurationSchemaWriter(final ConfigurationWriter writer) {
		this.writer = Objects.requireNonNull(writer);
	}

	/**
	 * Start a new key in the schema
	 *
	 * @param key      the schema key
	 * @param comments the property comments
	 * @return the schema property
	 */
	@Override
	public PropertyWriter with(final @NotNull String key, final @NotNull String... comments) {
		return new ConfigurationPropertyWriter(this, key, comments);
	}

	/**
	 * Build the schema
	 *
	 * @return the configuration writer
	 */
	@Override
	public ConfigWriter build() {
		return this.writer;
	}

	@Contract("null -> fail")
	public void apply(final MappingNode root) {
		Objects.requireNonNull(root);
		Map<String, Node> mapped = mapNode(root);

		for (Map.Entry<String, ConfigurationValueWriter<?>> entry : this.values.entrySet()) {
			String key = entry.getKey();
			ConfigurationValueWriter<?> value = entry.getValue();

			Node node = mapped.get(key);
			if (node == null) {

			}
		}
	}

	private static void eval(final ScalarNode key,
							 final ConfigurationValueWriter<?> value,
							 final MappingNode root,
							 final Node node) {
		Optional<?> constant = value.constant();
		if (node == null && constant.isPresent()) {
			Object obj = constant.get();

		}
	}

	private static Map<String, Node> mapNode(final MappingNode node) {
		return node.getValue().stream()
				.map(tuple -> {
					Node key = tuple.getKeyNode();
					if (!(key instanceof ScalarNode scalar)) {
						return null;
					}

					return Map.entry(scalar.getValue(), tuple.getValueNode());
				}).filter(Objects::nonNull).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
}
