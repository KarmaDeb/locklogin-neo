package com.github.karmadeb.locklogin.neo.common.i18n.registry;

import com.github.karmadeb.locklogin.neo.api.i18n.Localization;
import com.github.karmadeb.locklogin.neo.api.i18n.bundle.LocaleBundle;
import com.github.karmadeb.locklogin.neo.api.i18n.registry.LocaleDir;
import com.github.karmadeb.locklogin.neo.common.i18n.LocalizationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationRegistryTest {

	private final Path tempDir = Path.of("src", "test", "workdir", "i18n").toAbsolutePath();
	private LocalizationRegistry registry;

	@BeforeEach
	void setUp() throws IOException {
		registry = new LocalizationRegistry();
	}

	// --- append() tests ---

	@Test
	void append_addsLocalization() {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		Localization localization = new Localization() {
			@Override
			public void init(LocaleBundle bundle) {
				bundle.writer(Locale.ENGLISH).write("key", "value");
			}
		};

		registry.append(dir, localization);
		// Should not throw
	}

	@Test
	void append_nullDir_throwsNullPointerException() {
		Localization localization = new Localization() {
			@Override
			public void init(LocaleBundle bundle) {}
		};
		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> registry.append(null, localization));
	}

	@Test
	void append_nullLocalization_throwsNullPointerException() {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> registry.append(dir, null));
	}

	@Test
	void append_multipleLocalizations() {
		LocaleDir dir = LocaleDir.absolute(tempDir);

		Localization loc1 = new Localization() {
			@Override
			public void init(LocaleBundle bundle) {
				bundle.writer(Locale.ENGLISH).write("key1", "value1");
			}
		};

		Localization loc2 = new Localization() {
			@Override
			public void init(LocaleBundle bundle) {
				bundle.writer(Locale.ENGLISH).write("key2", "value2");
			}
		};

		registry.append(dir, loc1);
		registry.append(dir, loc2);
		// Should not throw
	}

	// --- bundle() tests ---

	@Test
	void bundle_returnsReader() throws IOException {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		Files.createDirectories(tempDir);

		var reader = registry.bundle(dir, Locale.ENGLISH);
		assertNotNull(reader);
		assertEquals(Locale.ENGLISH, reader.locale());
	}

	@Test
	void bundle_nullDir_throwsNullPointerException() {
		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> registry.bundle(null, Locale.ENGLISH));
	}

	@Test
	void bundle_nullLocale_throwsNullPointerException() {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> registry.bundle(dir, null));
	}

	// --- translate() tests ---

	@Test
	void translate_createsLocalizationFiles() throws IOException, ExecutionException, InterruptedException {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		Files.createDirectories(tempDir);

		// Create an initial localization file so the registry can read it
		Path enFile = tempDir.resolve("localization.en.yml");
		Files.writeString(enFile, "greeting: 'Hello'\n");

		Localization localization = new Localization() {
			@Override
			public void init(LocaleBundle bundle) {
				bundle.writer(Locale.ENGLISH).write("farewell", "Goodbye");
				bundle.writer(Locale.FRENCH).write("farewell", "Au revoir");
			}
		};

		registry.append(dir, localization);
		CompletableFuture<Void> future = registry.translate();
		future.get();

		// English file should exist and contain both keys
		assertTrue(Files.exists(enFile));
		String enContent = Files.readString(enFile);
		assertTrue(enContent.contains("greeting"));
		assertTrue(enContent.contains("farewell"));

		// French file should exist and contain the keys
		Path frFile = tempDir.resolve("localization.fr.yml");
		assertTrue(Files.exists(frFile));
		String frContent = Files.readString(frFile);
		// compose() should have copied keys from English to French
		assertTrue(frContent.contains("greeting"));
		assertTrue(frContent.contains("farewell"));
	}

	@Test
	void translate_emptyRegistry_completesSuccessfully() throws ExecutionException, InterruptedException {
		CompletableFuture<Void> future = registry.translate();
		future.get();
		// Should complete without error
	}

	@Test
	void translate_multipleDirectories() throws IOException, ExecutionException, InterruptedException {
		Path dir1 = tempDir.resolve("dir1");
		Path dir2 = tempDir.resolve("dir2");
		Files.createDirectories(dir1);
		Files.createDirectories(dir2);

		// Create initial files
		Files.writeString(dir1.resolve("localization.en.yml"), "key1: 'Value1'\n");
		Files.writeString(dir2.resolve("localization.en.yml"), "key2: 'Value2'\n");

		LocaleDir localeDir1 = LocaleDir.absolute(dir1);
		LocaleDir localeDir2 = LocaleDir.absolute(dir2);

		registry.append(localeDir1, new Localization() {
			@Override
			public void init(LocaleBundle bundle) {
				bundle.writer(Locale.ENGLISH).write("added1", "Added1");
			}
		});

		registry.append(localeDir2, new Localization() {
			@Override
			public void init(LocaleBundle bundle) {
				bundle.writer(Locale.ENGLISH).write("added2", "Added2");
			}
		});

		CompletableFuture<Void> future = registry.translate();
		future.get();

		// Both directories should have updated files
		String content1 = Files.readString(dir1.resolve("localization.en.yml"));
		assertTrue(content1.contains("key1"));
		assertTrue(content1.contains("added1"));

		String content2 = Files.readString(dir2.resolve("localization.en.yml"));
		assertTrue(content2.contains("key2"));
		assertTrue(content2.contains("added2"));
	}

	@Test
	void translate_withMultipleLocales() throws IOException, ExecutionException, InterruptedException {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		Files.createDirectories(tempDir);

		// Create initial files for multiple locales
		Files.writeString(tempDir.resolve("localization.en.yml"), "title: 'Welcome'\n");
		Files.writeString(tempDir.resolve("localization.fr.yml"), "title: 'Bienvenue'\n");

		registry.append(dir, new Localization() {
			@Override
			public void init(LocaleBundle bundle) {
				bundle.writer(Locale.ENGLISH).write("subtitle", "Hello");
			}
		});

		CompletableFuture<Void> future = registry.translate();
		future.get();

		// Both locales should have the subtitle key (via compose)
		String enContent = Files.readString(tempDir.resolve("localization.en.yml"));
		assertTrue(enContent.contains("subtitle"));

		String frContent = Files.readString(tempDir.resolve("localization.fr.yml"));
		assertTrue(frContent.contains("subtitle"));
	}

	@Test
	void translate_preservesExistingTranslations() throws IOException, ExecutionException, InterruptedException {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		Files.createDirectories(tempDir);

		Files.writeString(tempDir.resolve("localization.en.yml"), "greeting: 'Hello'\n");
		Files.writeString(tempDir.resolve("localization.fr.yml"), "greeting: 'Bonjour'\n");

		registry.append(dir, new Localization() {
			@Override
			public void init(LocaleBundle bundle) {
				// Adding a new key only in English
				bundle.writer(Locale.ENGLISH).write("newKey", "New value");
			}
		});

		CompletableFuture<Void> future = registry.translate();
		future.get();

		// French should keep its own greeting
		String frContent = Files.readString(tempDir.resolve("localization.fr.yml"));
		assertTrue(frContent.contains("Bonjour"));
	}

	@Test
	void translate_readsYamlFiles() throws IOException, ExecutionException, InterruptedException {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		Files.createDirectories(tempDir);

		// Create a YAML file with flat keys (NodeResolver uses cumulative keys)
		String yaml = "greeting: 'Hello'\nfarewell: 'Goodbye'\n";
		Files.writeString(tempDir.resolve("localization.en.yml"), yaml);

		registry.append(dir, new Localization() {
			@Override
			public void init(LocaleBundle bundle) {
				// Just read, don't write
			}
		});

		CompletableFuture<Void> future = registry.translate();
		future.get();

		// Verify the bundle can read the keys
		var reader = registry.bundle(dir, Locale.ENGLISH);
		assertTrue(reader.defined("greeting"));
		assertTrue(reader.defined("farewell"));
	}

	@Test
	void translate_handlesEmptyYamlFile() throws IOException, ExecutionException, InterruptedException {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		Files.createDirectories(tempDir);

		// Create an empty YAML file
		Files.writeString(tempDir.resolve("localization.en.yml"), "");

		registry.append(dir, new Localization() {
			@Override
			public void init(LocaleBundle bundle) {
				bundle.writer(Locale.ENGLISH).write("key", "value");
			}
		});

		CompletableFuture<Void> future = registry.translate();
		future.get();

		// Should still work
		String content = Files.readString(tempDir.resolve("localization.en.yml"));
		assertTrue(content.contains("key"));
	}

	@Test
	void translate_withLocaleVariants() throws IOException, ExecutionException, InterruptedException {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		Files.createDirectories(tempDir);

		// Create files with locale variants
		Files.writeString(tempDir.resolve("localization.en_US.yml"), "greeting: 'Hello US'\n");
		Files.writeString(tempDir.resolve("localization.en_GB.yml"), "greeting: 'Hello UK'\n");

		registry.append(dir, new Localization() {
			@Override
			public void init(LocaleBundle bundle) {
				bundle.writer(Locale.of("en", "US")).write("farewell", "Goodbye US");
			}
		});

		CompletableFuture<Void> future = registry.translate();
		future.get();

		// Both variant files should exist
		assertTrue(Files.exists(tempDir.resolve("localization.en_US.yml")));
		assertTrue(Files.exists(tempDir.resolve("localization.en_GB.yml")));
	}

	@Test
	void bundle_afterTranslate_returnsUpdatedReader() throws IOException, ExecutionException, InterruptedException {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		Files.createDirectories(tempDir);

		Files.writeString(tempDir.resolve("localization.en.yml"), "greeting: 'Hello'\n");

		registry.append(dir, new Localization() {
			@Override
			public void init(LocaleBundle bundle) {
				bundle.writer(Locale.ENGLISH).write("farewell", "Goodbye");
			}
		});

		registry.translate().get();

		var reader = registry.bundle(dir, Locale.ENGLISH);
		assertTrue(reader.defined("greeting"));
		assertTrue(reader.defined("farewell"));
	}
}
