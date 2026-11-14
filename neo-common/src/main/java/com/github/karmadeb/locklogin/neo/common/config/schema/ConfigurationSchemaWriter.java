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
import org.yaml.snakeyaml.nodes.SequenceNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
		ConfigurationWriter targetWriter = new ConfigurationWriter(root);
		Map<String, Node> mapped = mapNode(root);

		for (Map.Entry<String, ConfigurationValueWriter<?>> entry : this.values.entrySet()) {
			String key = entry.getKey();
			ConfigurationValueWriter<?> value = entry.getValue();

			Node node = mapped.get(key);
			if (node == null) {
				writeValue(value, targetWriter);
			} else {
				eval(value, root, node, targetWriter);
			}
		}
	}

	private void writeValue(final ConfigurationValueWriter<?> value, final ConfigurationWriter targetWriter) {
		String key = value.key();
		Object def = value.constant().orElse(null);

		if (def == null) {
			def = value.required().map(Supplier::get).orElse(null);
		}

		if (def == null) {
			def = value.with().map(Supplier::get).orElse(null);
		}

		if (def == null) {
			return;
		}

		if (def instanceof String s) {
			targetWriter.writeString(key, s);
		} else if (def instanceof Byte b) {
			targetWriter.writeByte(key, b);
		} else if (def instanceof Short s) {
			targetWriter.writeShort(key, s);
		} else if (def instanceof Integer i) {
			targetWriter.writeInt(key, i);
		} else if (def instanceof Long l) {
			targetWriter.writeLong(key, l);
		} else if (def instanceof Float f) {
			targetWriter.writeFloat(key, f);
		} else if (def instanceof Double d) {
			targetWriter.writeDouble(key, d);
		} else if (def instanceof Boolean b) {
			targetWriter.writeBool(key, b);
		} else if (def instanceof List<?> list) {
			writeListDefault(key, list, targetWriter);
		} else if (def instanceof SchemaWriter schema) {
			writeSectionDefault(key, (ConfigurationSchemaWriter) schema, targetWriter);
		}

		String[] comments = value.comment();
		if (comments != null && comments.length > 0) {
			targetWriter.comment(key, comments);
		}
	}

	@SuppressWarnings("unchecked")
	private void writeListDefault(final String key, final List<?> list, final ConfigurationWriter targetWriter) {
		if (list.isEmpty()) {
			targetWriter.writeStringList(key, new ArrayList<>());
			return;
		}

		Object first = list.getFirst();
		if (first instanceof String) {
			targetWriter.writeStringList(key, (List<String>) list);
		} else if (first instanceof SchemaWriter) {
			List<SchemaWriter> schemas = (List<SchemaWriter>) list;
			targetWriter.writeSections(key, supplier -> {
				List<ConfigWriter> writers = new ArrayList<>();
				for (SchemaWriter sw : schemas) {
					if (sw instanceof ConfigurationSchemaWriter cs) {
						ConfigWriter cw = supplier.get();
						if (cw instanceof ConfigurationWriter cwr) {
							cs.apply(cwr.yaml());
						}
						writers.add(cw);
					}
				}

				return writers;
			});
		}
	}

	private void writeSectionDefault(final String key, final ConfigurationSchemaWriter schema, final ConfigurationWriter targetWriter) {
		targetWriter.writeSection(key, childWriter -> {
			if (childWriter instanceof ConfigurationWriter cw) {
				schema.apply(cw.yaml());
			}
		});
	}

	private void eval(final ConfigurationValueWriter<?> value,
					  final MappingNode root,
					  final Node node,
					  final ConfigurationWriter targetWriter) {
		value.validator().ifPresent(validator -> {
			Predicate<Object> isInvalid = validator.negate();

			if (node instanceof ScalarNode scalar) {
				Object resolved = value.resolve(scalar);
				if (isInvalid.test(resolved)) {
					removeNode(root, value.key());
					writeValue(value, targetWriter);
				}
			} else if (node instanceof MappingNode mapping) {
				ConfigurationWriter writer = new ConfigurationWriter(mapping);
				if (isInvalid.test(writer)) {
					removeNode(root, value.key());
					writeValue(value, targetWriter);
				}
			} else if (node instanceof SequenceNode sequence) {
				List<Node> nodes = sequence.getValue();
				List<Object> transposed = new ArrayList<>();
				for (Node obj : nodes) {
					if (obj instanceof ScalarNode sc) {
						transposed.add(sc.getValue());
					} else if (obj instanceof MappingNode map) {
						ConfigurationWriter writer = new ConfigurationWriter(map);
						transposed.add(writer);
					}
				}

				if (isInvalid.test(transposed)) {
					removeNode(root, value.key());
					writeValue(value, targetWriter);
				}
			}
		});
	}

	private static void removeNode(final MappingNode root, final String key) {
		List<NodeTuple> tuples = root.getValue();
		for (int i = 0; i < tuples.size(); i++) {
			NodeTuple tuple = tuples.get(i);
			Node keyNode = tuple.getKeyNode();
			if (keyNode instanceof ScalarNode scalar && scalar.getValue().equals(key)) {
				tuples.remove(i);
				return;
			}
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
