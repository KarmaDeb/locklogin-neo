package com.github.karmadeb.locklogin.neo.common.config;

import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import com.github.karmadeb.locklogin.neo.common.config.schema.ConfigurationSchemaWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfigurationWriter extends ConfigurationReader implements ConfigWriter {

	protected final ConfigurationSchemaWriter schema;

	@Contract("null -> fail")
	public ConfigurationWriter(final MappingNode yamlDocument) {
		super(yamlDocument);
		this.schema = new ConfigurationSchemaWriter(this);
	}

	/**
	 * Set the comments for the specified
	 * key in the configuration. If the key does
	 * not exist when the configuration is generated,
	 * the comment is not added.
	 *
	 * @param key      the key
	 * @param comments the key comments
	 */
	@Override
	public void comment(final @NotNull String key, final @NotNull String... comments) {
		this.resolver.resolveKey(key).ifPresent(node -> node.setBlockComments(
				Arrays.stream(comments)
						.map(comment -> comment.startsWith(" ") ? comment :
								String.format(" %s", comment))
						.map(comment -> new CommentLine(null, null, comment, CommentType.BLOCK))
						.collect(Collectors.toList())
		));
	}

	/**
	 * Remove a configuration key
	 *
	 * @param key the key to remove
	 */
	@Override
	public void erase(final @NotNull String key) {
		this.resolver.resolveTuple(key, -2, false).ifPresent(tuple -> {
			AtomicReference<MappingNode> parent = new AtomicReference<>(this.yaml);
			String[] parts = key.split("\\.");

			for (int i = 0; i < parts.length - 1; i++) {
				Optional<Node> child = this.resolver.resolve(String.join(".", Arrays.copyOfRange(parts, 0, i + 1)));
				child.ifPresent(node -> {
					if (node instanceof MappingNode) {
						parent.set((MappingNode) node);
					}
				});
			}

			parent.get().getValue().removeIf(t -> t.getKeyNode() instanceof ScalarNode scalar &&
					scalar.getValue().equals(parts[parts.length - 1]));
		});
	}

	/**
	 * Write a string in the configuration
	 *
	 * @param key   the string key
	 * @param value the string value
	 */
	@Override
	public void writeString(final @NotNull String key, final @NotNull String value) {
		this.writeScalar(key, value, Tag.STR, ScalarStyle.SINGLE_QUOTED);
	}

	/**
	 * Write a byte in the configuration
	 *
	 * @param key   the byte key
	 * @param value the byte value
	 */
	@Override
	public void writeByte(final @NotNull String key, final @NotNull Byte value) {
		this.writeScalar(key, String.valueOf(value), Tag.INT, ScalarStyle.PLAIN);
	}

	/**
	 * Write a short in the configuration
	 *
	 * @param key   the short key
	 * @param value the short value
	 */
	@Override
	public void writeShort(final @NotNull String key, final @NotNull Short value) {
		this.writeScalar(key, String.valueOf(value), Tag.INT, ScalarStyle.PLAIN);
	}

	/**
	 * Write an int in the configuration
	 *
	 * @param key   the int key
	 * @param value the int value
	 */
	@Override
	public void writeInt(final @NotNull String key, final @NotNull Integer value) {
		this.writeScalar(key, String.valueOf(value), Tag.INT, ScalarStyle.PLAIN);
	}

	/**
	 * Write a long in the configuration
	 *
	 * @param key   the long key
	 * @param value the long value
	 */
	@Override
	public void writeLong(final @NotNull String key, final @NotNull Long value) {
		this.writeScalar(key, String.valueOf(value), Tag.INT, ScalarStyle.PLAIN);
	}

	/**
	 * Write a float in the configuration
	 *
	 * @param key   the float key
	 * @param value the float value
	 */
	@Override
	public void writeFloat(final @NotNull String key, final @NotNull Float value) {
		this.writeScalar(key, String.valueOf(value), Tag.FLOAT, ScalarStyle.PLAIN);
	}

	/**
	 * Write a double in the configuration
	 *
	 * @param key   the double key
	 * @param value the double value
	 */
	@Override
	public void writeDouble(final @NotNull String key, final @NotNull Double value) {
		this.writeScalar(key, String.valueOf(value), Tag.FLOAT, ScalarStyle.PLAIN);
	}

	/**
	 * Write a boolean in the configuration
	 *
	 * @param key   the boolean key
	 * @param value the boolean value
	 */
	@Override
	public void writeBool(final @NotNull String key, final @NotNull Boolean value) {
		this.writeScalar(key, String.valueOf(value), Tag.BOOL, ScalarStyle.PLAIN);
	}

	/**
	 * Write a section in the configuration
	 *
	 * @param key    the section key
	 * @param writer the section writer
	 */
	@Override
	public void writeSection(final @NotNull String key, final @NotNull Consumer<ConfigWriter> writer) {
		MappingNode newValue = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.BLOCK);
		ConfigWriter child = new ConfigurationWriter(newValue);

		this.resolver.resolve(key, -1, true).ifPresent(node -> {
			writer.accept(child);

			if (node instanceof MappingNode mapping) {
				ScalarNode keyNode = new ScalarNode(Tag.STR, key, null, null, ScalarStyle.PLAIN);
				mapping.getValue().add(new NodeTuple(keyNode, newValue));
			} else if (node instanceof SequenceNode sequence) {
				sequence.getValue().add(newValue);
			}
		});
	}

	/**
	 * Write a sections in the configuration
	 *
	 * @param key    the sections key
	 * @param writer the sections writer
	 */
	@Override
	public void writeSections(final @NotNull String key,
							  final @NotNull Function<Supplier<ConfigWriter>, List<ConfigWriter>> writer) {
		this.resolver.resolve(key, -1, true).ifPresent(node -> {
			List<ConfigWriter> configs = writer.apply(() -> {
				MappingNode newValue = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.BLOCK);
				return new ConfigurationWriter(newValue);
			});

			List<Node> nodes = configs.stream().map(w -> (ConfigurationWriter) w)
					.map(c -> c.yaml)
					.collect(Collectors.toList());

			SequenceNode seq = new SequenceNode(Tag.SEQ, nodes, FlowStyle.BLOCK);

			if (node instanceof MappingNode mapping) {
				ScalarNode keyNode = new ScalarNode(Tag.STR, key, null, null, ScalarStyle.PLAIN);

				mapping.getValue().add(new NodeTuple(keyNode, seq));
			} else if (node instanceof SequenceNode sequence) {
				sequence.getValue().add(seq);
			}
		});
	}

	/**
	 * Write a string list in the configuration
	 *
	 * @param key   the string list key
	 * @param value the string list value
	 */
	@Override
	public void writeStringList(final @NotNull String key, final @NotNull List<String> value) {
		this.resolver.resolve(key, -1, true).ifPresent(node -> {
			List<Node> transformed = value.stream()
					.map(s -> new ScalarNode(Tag.STR, s, null, null, ScalarStyle.SINGLE_QUOTED))
					.collect(Collectors.toList());
			SequenceNode seq = new SequenceNode(Tag.SEQ, transformed, FlowStyle.BLOCK);

			if (node instanceof MappingNode mapping) {
				ScalarNode keyNode = new ScalarNode(Tag.STR, key, null, null, ScalarStyle.PLAIN);
				mapping.getValue().add(new NodeTuple(keyNode, seq));
			} else if (node instanceof SequenceNode sequence) {
				sequence.getValue().add(seq);
			}
		});
	}

	/**
	 * Write a schema for this configuration writer
	 *
	 * @return the schema writer
	 */
	@Override
	public ConfigurationSchemaWriter schema() {
		return this.schema;
	}

	protected void writeScalar(final String key, final String value, final Tag tag, final ScalarStyle style) {
		ScalarNode valueNode = new ScalarNode(tag, value, null, null, style);

		this.resolver.resolve(key, -1, true).ifPresent(node -> {
			if (node instanceof MappingNode mapping) {
				ScalarNode keyNode = new ScalarNode(Tag.STR, key, null, null, ScalarStyle.PLAIN);
				mapping.getValue().add(new NodeTuple(keyNode, valueNode));
			} else if (node instanceof SequenceNode sequence) {
				if (sequence.getTag().equals(Tag.SET) && sequence.getValue()
						.stream()
						.filter(n -> n instanceof ScalarNode)
						.map(n -> (ScalarNode) n)
						.anyMatch(s -> s.getValue().equals(value))) {

					return;
				}

				sequence.getValue().add(valueNode);
			}
		});
	}
}
