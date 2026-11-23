package com.github.karmadeb.locklogin.neo.common.i18n.bundle;

import com.github.karmadeb.locklogin.neo.api.i18n.bundle.LocaleWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.nodes.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class LocalizationWriter extends LocalizationReader implements LocaleWriter {

	@Contract("null, _ -> fail; _, null -> fail")
	public LocalizationWriter(final Locale locale, final MappingNode yamlDocument) {
		super(locale, yamlDocument);
	}

	/**
	 * Remove a localization
	 *
	 * @param key the localization key
	 */
	@Override
	public void unset(final @NotNull String key) {
		this.resolver.resolveTuple(key, -1, false).ifPresent(_ -> {
			AtomicReference<MappingNode> parent = new AtomicReference<>(this.yaml);
			String[] parts = key.split("\\.");

			for (int i = 0; i < parts.length - 1; i++) {
				Optional<Node> child = this.resolver.resolve(String.join(".", Arrays.copyOfRange(parts, 0, i  +1)));
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
	 * Write a localization. If the key already exists the
	 * localization is not added
	 *
	 * @param key    the localization key
	 * @param phrase the localization value
	 */
	@Override
	public void write(final @NotNull String key, final @NotNull String phrase) {
		ScalarNode valueNode = new ScalarNode(Tag.STR, phrase, null, null, ScalarStyle.SINGLE_QUOTED);

		this.resolver.resolve(key, -1, true).ifPresent(node -> {
			if (node instanceof MappingNode mapping) {
				ScalarNode keyNode = new ScalarNode(Tag.STR, key, null, null, ScalarStyle.PLAIN);
				mapping.getValue().add(new NodeTuple(keyNode, valueNode));
			} else if (node instanceof SequenceNode sequence) {
				if (sequence.getTag().equals(Tag.SET) && sequence.getValue()
						.stream()
						.filter(n -> n instanceof ScalarNode)
						.map(n -> (ScalarNode) n)
						.anyMatch(s -> s.getValue().equals(phrase))) {

					return;
				}

				sequence.getValue().add(valueNode);
			}
		});
	}

	/**
	 * Write a localization list
	 *
	 * @param key     the localization key
	 * @param phrases the localization values
	 */
	@Override
	public void write(final @NotNull String key, final @NotNull Collection<String> phrases) {
		this.resolver.resolve(key, -1, true).ifPresent(node -> {
			List<Node> transformed = phrases.stream()
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

	public void copyTo(final LocalizationWriter writer) {
		copyEntries(this.yaml, writer.yaml);
	}

	private void copyEntries(final MappingNode source, final MappingNode target) {
		for (NodeTuple tuple : source.getValue()) {
			Node keyNode = tuple.getKeyNode();
			if (!(keyNode instanceof ScalarNode scalarKey)) {
				continue;
			}

			String key = scalarKey.getValue();

			boolean exists = false;
			for (NodeTuple targetTuple : target.getValue()) {
				Node tKey = targetTuple.getKeyNode();
				if (tKey instanceof ScalarNode tScalar && tScalar.getValue().equals(key)) {
					exists = true;
					break;
				}
			}

			if (exists) {
				continue;
			}

			Node valueNode = tuple.getValueNode();

			if (valueNode instanceof MappingNode sourceMapping) {
				MappingNode newMapping = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.BLOCK);
				ScalarNode newKeyNode = new ScalarNode(Tag.STR, key, null, null, ScalarStyle.PLAIN);
				target.getValue().add(new NodeTuple(newKeyNode, newMapping));
				copyEntries(sourceMapping, newMapping);
			} else {
				ScalarNode newKeyNode = new ScalarNode(Tag.STR, key, null, null, ScalarStyle.PLAIN);
				target.getValue().add(new NodeTuple(newKeyNode, valueNode));
			}
		}
	}

	/**
	 * Reorder the keys in this writer to match the order of the reference writer.
	 * Nested mappings are also reordered recursively.
	 */
	public void reorderKeys(final LocalizationWriter reference) {
		reorderEntries(this.yaml, reference.yaml);
	}

	private void reorderEntries(final MappingNode target, final MappingNode reference) {
		List<String> refKeys = new ArrayList<>();
		for (NodeTuple tuple : reference.getValue()) {
			if (tuple.getKeyNode() instanceof ScalarNode scalar) {
				refKeys.add(scalar.getValue());
			}
		}

		Map<String, NodeTuple> targetMap = new LinkedHashMap<>();
		for (NodeTuple tuple : target.getValue()) {
			if (tuple.getKeyNode() instanceof ScalarNode scalar) {
				targetMap.put(scalar.getValue(), tuple);
			}
		}

		List<NodeTuple> ordered = new ArrayList<>();
		for (String key : refKeys) {
			NodeTuple tuple = targetMap.remove(key);
			if (tuple != null) {
				ordered.add(tuple);
			}
		}

		ordered.addAll(targetMap.values());

		target.getValue().clear();
		target.getValue().addAll(ordered);

		for (String key : refKeys) {
			NodeTuple refTuple = findTuple(reference, key);
			NodeTuple tgtTuple = findTuple(target, key);
			if (refTuple != null && tgtTuple != null) {
				if (refTuple.getValueNode() instanceof MappingNode refMapping &&
					tgtTuple.getValueNode() instanceof MappingNode tgtMapping) {
					reorderEntries(tgtMapping, refMapping);
				}
			}
		}
	}

	private NodeTuple findTuple(final MappingNode node, final String key) {
		for (NodeTuple tuple : node.getValue()) {
			if (tuple.getKeyNode() instanceof ScalarNode scalar && scalar.getValue().equals(key)) {
				return tuple;
			}
		}

		return null;
	}
}
