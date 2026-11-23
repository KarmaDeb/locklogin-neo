package com.github.karmadeb.locklogin.neo.common;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class NodeResolver {

	public static final class Result {
		Node node;
		NodeTuple tuple;
		Result(Node node, NodeTuple tuple) {
			this.node = node;
			this.tuple = tuple;
		}
	}

	private final MappingNode node;

	@Contract("null -> fail")
	public NodeResolver(final MappingNode node) {
		this.node = Objects.requireNonNull(node);
	}

	public Optional<Node> resolve(final @NotNull String key) {
		return this.resolve(key, 0, false);
	}

	public Optional<ScalarNode> resolveKey(final @NotNull String key) {
		return this.resolveKey(key, 0, false);
	}

	public Optional<Node> resolve(final @NotNull String key, final int level, final boolean fill) {
		Result result = this.resolveInternal(key, level, fill);
		if (result.tuple != null) {
			return Optional.of(result.tuple.getValueNode());
		}

		return Optional.ofNullable(result.node);
	}

	@SuppressWarnings("SameParameterValue")
	public Optional<ScalarNode> resolveKey(final @NotNull String key, final int level, final boolean fill) {
		Result result = this.resolveInternal(key, level, fill);
		if (result.tuple != null) {
			Node keyNode = result.tuple.getKeyNode();
			if (keyNode instanceof ScalarNode scalar) {
				return Optional.of(scalar);
			}
		}
		return Optional.empty();
	}

	@SuppressWarnings("unused")
	public Optional<NodeTuple> resolveTuple(final @NotNull String key, final int level, final boolean fill) {
		Result result = this.resolveInternal(key, level, fill);
		return Optional.ofNullable(result.tuple);
	}

	public Result resolveInternal(final @NotNull String key, final int level, final boolean fill) {
		String[] parts = key.split("\\.");
		StringBuilder keyBuilder = new StringBuilder();

		Node node = this.node;
		NodeTuple lastTuple = null;
		iter:
		for (int i = 0; i < (parts.length + level); i++) {
			String part = parts[i];

			if (!keyBuilder.isEmpty()) {
				keyBuilder.append(".");
			}

			keyBuilder.append(part);
			String tmpKey = keyBuilder.toString();

			if (node instanceof MappingNode mapping) {
				for (NodeTuple tuple : mapping.getValue()) {
					Node keyNode = tuple.getKeyNode();
					if (keyNode instanceof ScalarNode scalar && scalar.getValue().equals(tmpKey)) {
						node = tuple.getValueNode();
						lastTuple = tuple;

						continue iter;
					}
				}
			}

			if (fill) {
				Node newNode = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
				if (node instanceof MappingNode mapping) {
					List<NodeTuple> tuples = mapping.getValue();

					ScalarNode keyScalar = new ScalarNode(Tag.STR, key, null, null,
							ScalarStyle.PLAIN);
					NodeTuple newTuple = new NodeTuple(keyScalar, newNode);
					tuples.add(newTuple);
					lastTuple = newTuple;
				} else if (node instanceof SequenceNode sequence) {
					List<Node> values = sequence.getValue();
					values.add(newNode);
				} else {
					return new Result(null, null);
				}

				node = newNode;
				continue;
			}

			return new Result(null, null);
		}

		return new Result(node, lastTuple);
	}
}
