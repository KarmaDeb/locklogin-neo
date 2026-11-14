package com.github.karmadeb.locklogin.neo.common.config.registry;

import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPart;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPath;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPath.Walker;
import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigFile;
import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigRegistry;
import com.github.karmadeb.locklogin.neo.common.config.ConfigurationWriter;
import com.github.karmadeb.locklogin.neo.common.config.schema.ConfigurationSchemaWriter;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ConfigurationRegistry extends ConfigRegistry {

	private static final LoaderOptions LOADER_OPTIONS = new LoaderOptions(){{
		setAllowDuplicateKeys(false);
	}}.setProcessComments(true);
	private static final DumperOptions DUMPER_OPTIONS = new DumperOptions(){{
		setProcessComments(true);
		setPrettyFlow(true);
		setAllowUnicode(true);

		setIndent(2);
		setIndicatorIndent(2);
		setIndentWithIndicator(true);
		setSplitLines(true);

		setDefaultFlowStyle(FlowStyle.BLOCK);
		setDefaultScalarStyle(ScalarStyle.SINGLE_QUOTED);
	}};
	private static final Yaml YAML = new Yaml(LOADER_OPTIONS, DUMPER_OPTIONS);

	private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
	private final Map<ConfigFile, LinkedHashSet<ConfigPart>> configurations = new ConcurrentHashMap<>();

	/**
	 * Append a part to a configuration file
	 *
	 * @param file the file to append
	 * @param part the part to append
	 */
	@Override
	public void append(final @NotNull ConfigFile file, final @NotNull ConfigPart part) {
		Objects.requireNonNull(file);
		Objects.requireNonNull(part);

		LinkedHashSet<ConfigPart> parts = configurations.computeIfAbsent(file, _ -> new LinkedHashSet<>());
		parts.addFirst(part);
	}

	/**
	 * Get all the parts of a configuration file
	 *
	 * @param file the file parts
	 * @return the parts
	 */
	@Override
	public ConfigPart @NotNull [] parts(final @NotNull ConfigFile file) {
		if (!configurations.containsKey(file)) {
			return new ConfigPart[0];
		}

		return Collections.unmodifiableCollection(
				configurations.get(file)
		).toArray(new ConfigPart[0]);
	}

	public CompletableFuture<Void> configure() {
		List<CompletableFuture<Void>> tasks = new ArrayList<>();

		for (Map.Entry<ConfigFile, LinkedHashSet<ConfigPart>> entry : this.configurations.entrySet()) {
			ConfigFile file = entry.getKey();
			LinkedHashSet<ConfigPart> parts = entry.getValue();

			// Virtual threads for I/O operations are extremely useful
			tasks.add(CompletableFuture.runAsync(() -> mapConfigFile(file, parts), EXECUTOR));
		}

		return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
	}

	private static void mapConfigFile(final ConfigFile file, final LinkedHashSet<ConfigPart> parts) {
		MappingNode master;

		try(InputStream in = file.read()) {
			try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
				Node resolved = YAML.compose(reader);
				if (!(resolved instanceof MappingNode)) {
					resolved = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
				}

				master = (MappingNode) resolved;
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		Map<ConfigurationSchemaWriter, MappingNode> schemas = new HashMap<>();
		for (ConfigPart part : parts) {
			ConfigPath path = part.path();
			MappingNode node = master;
			Walker walker = path.navigate();
			while (walker != null) {
				node = navigateTo(node, walker.from());
				walker = walker.to();
			}

			ConfigurationWriter writer = new ConfigurationWriter(node);
			part.init(writer);

			schemas.put(writer.schema(), node);
		}

		for (Map.Entry<ConfigurationSchemaWriter, MappingNode> entry : schemas.entrySet()) {
			ConfigurationSchemaWriter writer = entry.getKey();
			MappingNode value = entry.getValue();

			writer.apply(value);
		}

 		try (OutputStream out = file.write();
			 OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
			StringWriter sw = new StringWriter();
			YAML.serialize(master, sw);
			String yaml = sw.toString();

			writer.write(insertSiblingSeparators(yaml));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Insert blank lines between sibling keys in a YAML document.
	 * A sibling is a key or list item that appears at the same indentation level
	 * as its predecessor.
	 */
	private static String insertSiblingSeparators(final String yaml) {
		String[] lines = yaml.split("\n", -1);
		boolean[] insertBefore = new boolean[lines.length];

		for (int i = 0; i < lines.length; i++) {
			String trimmed = lines[i].trim();

			if (trimmed.isEmpty()) {
				continue;
			}

			int prevIdx = i - 1;
			while (prevIdx >= 0) {
				String prevTrim = lines[prevIdx].trim();
				if (!prevTrim.isEmpty() && !prevTrim.startsWith("#")) {
					break;
				}
				prevIdx--;
			}
			if (prevIdx < 0) {
				continue;
			}

			String prevNonEmpty = lines[prevIdx];
			String prevTrimmed = prevNonEmpty.trim();

			int prevIndent = getIndent(prevNonEmpty);
			int currIndent = getIndent(lines[i]);

			if (currIndent <= prevIndent && currIndent >= 0) {
				boolean currIsList = trimmed.startsWith("- ") || trimmed.startsWith("-'") || trimmed.startsWith("-\"") || trimmed.equals("-");
				boolean prevIsList = prevTrimmed.startsWith("- ") || prevTrimmed.startsWith("-'") || prevTrimmed.startsWith("-\"") || prevTrimmed.equals("-");

				if (!(currIsList && prevIsList && currIndent == prevIndent)) {
					if (currIsList || trimmed.contains(":")) {
						boolean hasBlankBetween = false;
						for (int b = prevIdx + 1; b < i; b++) {
							if (lines[b].trim().isEmpty()) {
								hasBlankBetween = true;
								break;
							}
						}
						if (!hasBlankBetween) {
							int markIdx = getMarkIdx(i, trimmed, lines);
							insertBefore[markIdx] = true;
						}
					}
				}
			}
		}

		StringBuilder result = new StringBuilder(yaml.length() * 2);
		for (int i = 0; i < lines.length; i++) {
			if (insertBefore[i]) {
				result.append("\n");
			}
			if (i > 0) {
				result.append("\n");
			}
			result.append(lines[i]);
		}

		return result.toString();
	}

	private static int getMarkIdx(int i, String trimmed, String[] lines) {
		int markIdx = i;
		if (trimmed.startsWith("#")) {
			while (markIdx > 0 && lines[markIdx - 1].trim().startsWith("#")) {
				markIdx--;
			}
		} else {
			int beforeIdx = i - 1;
			while (beforeIdx >= 0 && lines[beforeIdx].trim().isEmpty()) {
				beforeIdx--;
			}
			if (beforeIdx >= 0 && lines[beforeIdx].trim().startsWith("#")) {
				while (beforeIdx > 0 && lines[beforeIdx - 1].trim().startsWith("#")) {
					beforeIdx--;
				}
				markIdx = beforeIdx;
			}
		}
		return markIdx;
	}

	/**
	 * Returns the indentation level (number of leading spaces) of a line.
	 * Returns -1 for empty lines.
	 */
	private static int getIndent(final String line) {
		if (line.trim().isEmpty()) {
			return -1;
		}
		int indent = 0;
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == ' ') {
				indent++;
			} else {
				break;
			}
		}
		return indent;
	}

	private static MappingNode navigateTo(final MappingNode from, final String key) {
		List<NodeTuple> values = from.getValue();
		NodeTuple[] tuples = values.toArray(new NodeTuple[0]);

		ScalarNode keyNode = new ScalarNode(Tag.STR, key, null, null, ScalarStyle.PLAIN);
		MappingNode node = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);

		for (int i = 0; i < tuples.length; i++) {
			NodeTuple tuple = tuples[i];
			Node tupleKey = tuple.getKeyNode();
			if (!(tupleKey instanceof ScalarNode scalarKey) || !scalarKey.getValue().equals(key)) {
				continue;
			}

			Node value = tuple.getValueNode();
			if (!(value instanceof MappingNode mapping)) {
				values.set(i, new NodeTuple(keyNode, node));
				return node;
			}

			return mapping;
		}

		values.add(new NodeTuple(keyNode, node));
		return node;
	}
}
