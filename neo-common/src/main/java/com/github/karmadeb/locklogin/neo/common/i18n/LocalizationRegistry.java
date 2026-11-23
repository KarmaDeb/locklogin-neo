package com.github.karmadeb.locklogin.neo.common.i18n;

import com.github.karmadeb.locklogin.neo.api.i18n.Localization;
import com.github.karmadeb.locklogin.neo.api.i18n.bundle.LocaleReader;
import com.github.karmadeb.locklogin.neo.api.i18n.registry.LocaleDir;
import com.github.karmadeb.locklogin.neo.api.i18n.registry.LocaleRegistry;
import com.github.karmadeb.locklogin.neo.common.YamlPrettyFormatter;
import com.github.karmadeb.locklogin.neo.common.i18n.bundle.LocalizationBundle;
import com.github.karmadeb.locklogin.neo.common.i18n.bundle.LocalizationReader;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class LocalizationRegistry extends LocaleRegistry {

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
	private final Map<LocaleDir, LinkedHashSet<Localization>> localizations = new ConcurrentHashMap<>();
	private final Map<LocaleDir, LocalizationBundle> bundles = new ConcurrentHashMap<>();

	/**
	 * Append a part to a configuration file
	 *
	 * @param path    the localizations directory
	 * @param locales the locales
	 */
	@Override
	public void append(final @NotNull LocaleDir path, final @NotNull Localization locales) {
		Objects.requireNonNull(path);
		Objects.requireNonNull(locales);

		LinkedHashSet<Localization> parts = localizations.computeIfAbsent(path, _ -> new LinkedHashSet<>());
		parts.add(locales);
	}

	/**
	 * Obtain a new localization reader. Unlike configuration, localization
	 * is stored as a single unit of work in the whole directory. If a locale
	 * does not exist inside the {@link LocaleDir}, the first available is tried.
	 *
	 * @param dir    the locale directory
	 * @param locale the locale
	 * @return the locale reader
	 */
	@Override
	public @NotNull LocaleReader bundle(final @NotNull LocaleDir dir, final @NotNull Locale locale) {
		LocalizationBundle bundle = bundles.computeIfAbsent(dir, _ ->
				new LocalizationBundle(Collections.emptyMap()));
		return bundle.reader(locale);
	}

	public CompletableFuture<Void> translate() {
		List<CompletableFuture<Void>> tasks = new ArrayList<>();

		for (Map.Entry<LocaleDir, LinkedHashSet<Localization>> entry : this.localizations.entrySet()) {
			LocaleDir dir = entry.getKey();
			LinkedHashSet<Localization> locales = entry.getValue();

			// Virtual threads for I/O operations are extremely useful
			CompletableFuture<Void> task = new CompletableFuture<>();
			EXECUTOR.submit(() -> {
				try {
					mapLocalizationDirectory(dir, locales);
					task.complete(null);
				} catch (Throwable ex) {
					task.completeExceptionally(ex);
				}
			});

			tasks.add(task);
		}

		return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
	}

	private void mapLocalizationDirectory(final LocaleDir dir, final LinkedHashSet<Localization> parts) throws IOException {
		Path path = dir.path();

		Map<Locale, LocalizationReader> readerMap = new LinkedHashMap<>();
		try (Stream<Path> s = Files.list(path).filter(Files::isRegularFile).filter(f -> {
			String name = f.getFileName().toString();
			return name.matches("^localization\\.([a-zA-Z]+(_[a-zA-Z]+){0,2}).ya?ml$");
		})) {
			for (Path file : s.toList()) {
				String name = file.getFileName().toString();
				String[] code = name.split("\\.")[1].split("_");

				Locale locale;
				if (code.length == 1) {
					locale = Locale.of(code[0]);
				} else if (code.length == 2) {
					locale = Locale.of(code[0], code[1]);
				} else {
					locale = Locale.of(code[0], code[1], code[2]);
				}

				try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
					Node resolved = YAML.compose(reader);
					if (!(resolved instanceof MappingNode)) {
						resolved = new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
					}

					readerMap.put(locale, new LocalizationReader(locale, (MappingNode) resolved));
				}
			}
		}

		LocalizationBundle bundle = new LocalizationBundle(readerMap);
		for (Localization localization : parts) {
			localization.init(bundle);
		}

		bundle.compose();
		bundles.put(dir, bundle);

		for (Locale locale : bundle.locales()) {
			LocalizationReader reader = bundle.reader(locale);
			try (OutputStream out = dir.write(locale);
				 OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
				StringWriter sw = new StringWriter();
				YAML.serialize(reader.yaml(), sw);
				String yaml = sw.toString();

				writer.write(YamlPrettyFormatter.format(yaml));
			}
		}
	}
}
