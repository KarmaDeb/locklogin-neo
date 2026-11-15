package com.github.karmadeb.locklogin.neo.common.config.registry;

import com.github.karmadeb.locklogin.neo.api.configuration.ConfigReader;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPart;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPath;
import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigFile;
import com.github.karmadeb.locklogin.neo.api.configuration.schema.SchemaWriter;
import com.github.karmadeb.locklogin.neo.common.config.schema.ConfigurationRegistry;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationRegistryTest {

	private final Path tempDir = Path.of("src", "test", "workdir").toAbsolutePath();
	private ConfigurationRegistry registry;

	@BeforeEach
	void setUp() throws IOException {
		registry = new ConfigurationRegistry();
		// Ensure temp directory exists
		Files.createDirectories(tempDir);
	}

	// --- append() tests ---

	@Test
	void append_addsPart_toFile() {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test.yaml"));
		ConfigPart part = new TestPart(ConfigPath.empty());

		registry.append(file, part);

		assertArrayEquals(new ConfigPart[]{part}, registry.parts(file));
	}

	@Test
	void append_throwsOnNullFile() {
		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> registry.append(null, new TestPart(ConfigPath.empty())));
	}

	@Test
	void append_throwsOnNullPart() {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test.yaml"));
		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> registry.append(file, null));
	}

	@Test
	void append_multipleParts_preservesOrder() {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.of("section1"));
		ConfigPart part2 = new TestPart(ConfigPath.of("section2"));

		registry.append(file, part1);
		registry.append(file, part2);

		// addFirst means latest part comes first
		ConfigPart[] parts = registry.parts(file);
		assertEquals(2, parts.length);
		assertSame(part2, parts[0]);
		assertSame(part1, parts[1]);
	}

	@Test
	void append_samePath_mergesParts() {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-merge.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("name").string(v -> v.with(() -> "defaultName"));
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port").intNumber(v -> v.with(() -> 8080));
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		// The two parts should be merged into a single part
		ConfigPart[] parts = registry.parts(file);
		assertEquals(1, parts.length);
	}

	@Test
	void append_samePath_mergedSchema_appliesBothValues() throws IOException {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-merge-values.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("name").string(v -> v.with(() -> "defaultName"));
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port").intNumber(v -> v.with(() -> 8080));
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		// Verify file was created with both values
		Path filePath = tempDir.resolve("test-merge-values.yaml");
		assertTrue(Files.exists(filePath), "Config file should be created");

		String content = Files.readString(filePath);
		assertTrue(content.contains("defaultName"), "File should contain name from part1: " + content);
		assertTrue(content.contains("8080"), "File should contain port from part2: " + content);
	}

	@Test
	void append_samePath_overlappingKey_secondWins() throws IOException {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-merge-overlap.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("name").string(v -> v.with(() -> "original"));
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("name").string(v -> v.with(() -> "overridden"));
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		// Verify the second part's value wins for the overlapping key
		Path filePath = tempDir.resolve("test-merge-overlap.yaml");
		String content = Files.readString(filePath);
		assertTrue(content.contains("overridden"), "File should contain overridden value: " + content);
		assertFalse(content.contains("original"), "File should not contain original value: " + content);
	}

	@Test
	void append_samePath_nestedSection_mergesSchemas() throws IOException {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-merge-nested.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.of("server")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("host").string(v -> v.with(() -> "localhost"));
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.of("server")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port").intNumber(v -> v.with(() -> 9090));
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		// Verify both values are in the server section
		Path filePath = tempDir.resolve("test-merge-nested.yaml");
		String content = Files.readString(filePath);
		assertTrue(content.contains("server"), "Should contain server section: " + content);
		assertTrue(content.contains("localhost"), "Should contain host from part1: " + content);
		assertTrue(content.contains("9090"), "Should contain port from part2: " + content);
	}

	// --- parts() tests ---

	@Test
	void parts_returnsEmptyForUnknownFile() {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("unknown.yaml"));

		ConfigPart[] parts = registry.parts(file);

		assertEquals(0, parts.length);
	}

	@Test
	void parts_returnsCopy_notInternalArray() {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test.yaml"));
		ConfigPart part = new TestPart(ConfigPath.empty());
		registry.append(file, part);

		ConfigPart[] parts = registry.parts(file);
		assertEquals(1, parts.length);
		assertSame(part, parts[0]);
	}

	// --- configure() tests ---

	@Test
	void configure_createsFile_withDefaults() throws IOException {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-defaults.yaml"));
		ConfigPart part = new TestPart() {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("name", "The display name for this configuration").string(v -> v.with(() -> "defaultName"));
				writer.schema().with("port", "The server port", "Must be between 1024 and 65535").intNumber(v -> v.with(() -> 8080));
			}
		};

		registry.append(file, part);
		registry.configure().join();

		// Verify file was created
		Path filePath = tempDir.resolve("test-defaults.yaml");
		assertTrue(Files.exists(filePath), "Config file should be created");

		// Verify file content
		String content = Files.readString(filePath);
		assertTrue(content.contains("defaultName"), "File should contain default name: " + content);
		assertTrue(content.contains("8080"), "File should contain default port: " + content);
		assertTrue(content.contains("# The display name"), "File should contain name comment: " + content);
		assertTrue(content.contains("# The server port"), "File should contain port comment: " + content);
		assertTrue(content.contains("# Must be between"), "File should contain port constraint comment: " + content);
	}

	@Test
	void configure_createsNestedDirectory() throws IOException {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("sub", "dir", "nested-dir.yaml"));
		ConfigPart part = new TestPart() {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("key", "A key in a nested directory file").string(v -> v.with(() -> "value"));
			}
		};

		registry.append(file, part);
		registry.configure().join();

		// Verify file was created in nested directory
		Path filePath = tempDir.resolve("sub/dir/nested-dir.yaml");
		assertTrue(Files.exists(filePath), "Config file should be created in nested directory");

		String content = Files.readString(filePath);
		assertTrue(content.contains("# A key in a nested directory file"), "File should contain comment: " + content);
	}

	@Test
	void configure_withConfigPath_navigatesToSection() throws IOException {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-section.yaml"));
		ConfigPart part = new TestPart(ConfigPath.of("server", "network")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("host", "The server hostname").string(v -> v.with(() -> "localhost"));
				writer.schema().with("port", "The server port number").intNumber(v -> v.with(() -> 9090));
			}
		};

		registry.append(file, part);
		registry.configure().join();

		Path filePath = tempDir.resolve("test-section.yaml");
		assertTrue(Files.exists(filePath));

		String content = Files.readString(filePath);
		assertTrue(content.contains("server"), "Should contain server section: " + content);
		assertTrue(content.contains("network"), "Should contain network section: " + content);
		assertTrue(content.contains("localhost"), "Should contain host value: " + content);
		assertTrue(content.contains("9090"), "Should contain port value: " + content);
		assertTrue(content.contains("# The server hostname"), "Should contain host comment: " + content);
		assertTrue(content.contains("# The server port number"), "Should contain port comment: " + content);
	}

	@Test
	void configure_preservesExistingValues() throws IOException {
		// Pre-create a config file with existing values
		Path configPath = tempDir.resolve("test-preserve.yaml");
		Files.writeString(configPath, "name: 'existingName'\nport: 3000\n");

		ConfigFile file = ConfigFile.absolute(configPath);
		ConfigPart part = new TestPart() {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				// Defaults should not overwrite existing values
				writer.schema().with("name", "The configuration name").string(v -> v.with(() -> "defaultName"));
				writer.schema().with("port", "The port number").intNumber(v -> v.with(() -> 8080));
				// New key with default and comment
				writer.schema().with("enabled", "Whether the feature is enabled").bool(v -> v.with(() -> true));
			}
		};

		registry.append(file, part);
		registry.configure().join();

		// Verify existing values are preserved
		String content = Files.readString(configPath);
		assertTrue(content.contains("existingName"), "Existing name should be preserved: " + content);
		assertTrue(content.contains("3000"), "Existing port should be preserved: " + content);
		assertTrue(content.contains("enabled"), "New key should be added: " + content);
		assertTrue(content.contains("# Whether the feature is enabled"), "New key should have comment: " + content);
	}

	@Test
	void configure_withValidator_replacesInvalidValue() throws IOException {
		// Pre-create a config file with an invalid value
		Path configPath = tempDir.resolve("test-validator.yaml");
		Files.writeString(configPath, "port: -1\n");

		ConfigFile file = ConfigFile.absolute(configPath);
		ConfigPart part = new TestPart() {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server listening port", "Must be a positive integer").intNumber(v -> {
					v.with(() -> 8080);
					v.validator(obj -> obj instanceof Integer && (Integer) obj > 0);
				});
			}
		};

		registry.append(file, part);
		// configure should replace the invalid value with the default
		registry.configure().join();

		// Invalid value should be replaced with the default
		String content = Files.readString(configPath);
		assertTrue(content.contains("8080"), "Invalid port should be replaced with default: " + content);
		assertFalse(content.contains("-1"), "Invalid value should be removed: " + content);
		assertTrue(content.contains("# Server listening port"), "Should contain port comment: " + content);
		assertTrue(content.contains("# Must be a positive integer"), "Should contain validator comment: " + content);
	}

	@Test
	void configure_withValidator_preservesValidValue() throws IOException {
		// Pre-create a config file with a valid value
		Path configPath = tempDir.resolve("test-validator-valid.yaml");
		Files.writeString(configPath, "port: 8085\n");

		ConfigFile file = ConfigFile.absolute(configPath);
		ConfigPart part = new TestPart() {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server listening port").intNumber(v -> {
					v.with(() -> 8080);
					v.validator(obj -> obj instanceof Integer && (Integer) obj > 0);
				});
			}
		};

		registry.append(file, part);
		// configure should preserve the valid value
		registry.configure().join();

		// Valid value should be preserved, not replaced with default
		String content = Files.readString(configPath);
		assertTrue(content.contains("8085"), "Valid port should be preserved: " + content);
		assertFalse(content.contains("8080"), "Default should not overwrite valid value: " + content);
	}

	@Test
	void configure_withValidator_required_preservesValidValue() throws IOException {
		// Pre-create a config file with a valid value
		// Uses required() instead of with() — matches production code pattern
		Path configPath = tempDir.resolve("test-validator-required.yaml");
		Files.writeString(configPath, "cost: 12\n");

		ConfigFile file = ConfigFile.absolute(configPath);
		ConfigPart part = new TestPart() {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("cost", "Resource cost", "Must be at least 10").intNumber(v -> {
					v.required(10);
					v.validator(obj -> obj instanceof Number n && n.intValue() >= 10);
				});
			}
		};

		registry.append(file, part);
		// configure should preserve the valid value
		registry.configure().join();

		// Valid value should be preserved, not replaced with default
		String content = Files.readString(configPath);
		assertTrue(content.contains("12"), "Valid cost should be preserved: " + content);
		assertFalse(content.contains("10"), "Default should not overwrite valid value: " + content);
	}

	@Test
	void configure_multipleParts_sameFile() throws IOException {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-multi-parts.yaml"));

		ConfigPart part1 = new TestPart(ConfigPath.of("plugin1")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("enabled", "Whether plugin1 is enabled").bool(v -> v.with(() -> true)
						.validator(val -> val instanceof Boolean));
			}
		};

		ConfigPart part2 = new TestPart(ConfigPath.of("plugin2")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("name", "The display name for plugin2").string(v -> v.with(() -> "Plugin2"));
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		Path filePath = tempDir.resolve("test-multi-parts.yaml");
		assertTrue(Files.exists(filePath));

		String content = Files.readString(filePath);
		// Both parts create their own sections with values under the root
		assertTrue(content.contains("plugin1"), "Should contain plugin1 section: " + content);
		assertTrue(content.contains("plugin2"), "Should contain plugin2 section: " + content);
		assertTrue(content.contains("enabled"), "Should contain enabled key: " + content);
		assertTrue(content.contains("true"), "Should contain enabled value true: " + content);
		assertTrue(content.contains("Plugin2"), "Should contain Plugin2 value: " + content);
		assertTrue(content.contains("# Whether plugin1 is enabled"), "Should contain plugin1 comment: " + content);
		assertTrue(content.contains("# The display name for plugin2"), "Should contain plugin2 comment: " + content);
	}

	@Test
	void configure_multipleFiles() throws IOException {
		ConfigFile file1 = ConfigFile.absolute(tempDir.resolve("test-multi-file1.yaml"));
		ConfigFile file2 = ConfigFile.absolute(tempDir.resolve("test-multi-file2.yaml"));

		ConfigPart part1 = new TestPart() {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("key1", "First configuration key").string(v -> v.with(() -> "value1"));
			}
		};

		ConfigPart part2 = new TestPart() {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("key2", "Second configuration key").string(v -> v.with(() -> "value2"));
			}
		};

		registry.append(file1, part1);
		registry.append(file2, part2);
		registry.configure().join();

		assertTrue(Files.exists(tempDir.resolve("test-multi-file1.yaml")));
		assertTrue(Files.exists(tempDir.resolve("test-multi-file2.yaml")));

		String content1 = Files.readString(tempDir.resolve("test-multi-file1.yaml"));
		String content2 = Files.readString(tempDir.resolve("test-multi-file2.yaml"));

		assertTrue(content1.contains("value1"), "Config1 should contain value1: " + content1);
		assertTrue(content2.contains("value2"), "Config2 should contain value2: " + content2);
		assertTrue(content1.contains("# First configuration key"), "Config1 should contain comment: " + content1);
		assertTrue(content2.contains("# Second configuration key"), "Config2 should contain comment: " + content2);
	}

	@Test
	void configure_withConstant_writesConstantValue() throws IOException {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-constant.yaml"));
		ConfigPart part = new TestPart() {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("version", "Plugin version", "Do not modify this value").string(v -> v.constant("1.0.0"));
			}
		};

		registry.append(file, part);
		registry.configure().join();

		String content = Files.readString(tempDir.resolve("test-constant.yaml"));
		assertTrue(content.contains("1.0.0"), "Constant value should be written: " + content);
		assertTrue(content.contains("# Plugin version"), "Should contain version comment: " + content);
		assertTrue(content.contains("# Do not modify"), "Should contain warning comment: " + content);
	}

	@Test
	void configure_withStringList() throws IOException {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-list.yaml"));
		ConfigPart part = new TestPart() {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("tags", "List of tags for categorization").list(v -> v.with(() -> List.of("alpha", "beta", "gamma")));
			}
		};

		registry.append(file, part);
		registry.configure().join();

		String content = Files.readString(tempDir.resolve("test-list.yaml"));
		assertTrue(content.contains("alpha"), "Should contain alpha: " + content);
		assertTrue(content.contains("beta"), "Should contain beta: " + content);
		assertTrue(content.contains("gamma"), "Should contain gamma: " + content);
		assertTrue(content.contains("# List of tags"), "Should contain tags comment: " + content);
	}

	@Test
	void configure_createsSectionFromPath() throws IOException {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-section-path.yaml"));
		ConfigPart part = new TestPart(ConfigPath.of("database", "connection")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("url", "Database connection URL").string(v -> v.with(() -> "jdbc:mysql://localhost:3306/db"));
				writer.schema().with("timeout", "Connection timeout in seconds").intNumber(v -> v.with(() -> 30));
			}
		};

		registry.append(file, part);
		registry.configure().join();

		String content = Files.readString(tempDir.resolve("test-section-path.yaml"));
		assertTrue(content.contains("database"), "Should contain database section: " + content);
		assertTrue(content.contains("connection"), "Should contain connection section: " + content);
		assertTrue(content.contains("jdbc:mysql"), "Should contain URL: " + content);
		assertTrue(content.contains("30"), "Should contain timeout: " + content);
		assertTrue(content.contains("# Database connection URL"), "Should contain URL comment: " + content);
		assertTrue(content.contains("# Connection timeout"), "Should contain timeout comment: " + content);
	}

	@Test
	void configure_complexNestedStructure() throws IOException {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-complex-nested.yaml"));

		// Top-level part with scalars, lists, and all numeric types
		ConfigPart topPart = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("appName", "The application display name").string(v1 -> v1.with(() -> "MyApp"));
				writer.schema().with("version", "Application version", "Do not modify manually").string(v2 -> v2.constant("2.0.0"));
				writer.schema().with("tags", "Application tags for categorization").list(v3 -> v3.with(() -> List.of("production", "web", "api")));
				writer.schema().with("maxMemory", "Maximum memory allocation in MB").intNumber(v52 -> v52.with(() -> 2048));
				writer.schema().with("logLevel", "Log level verbosity").byteNumber(v53 -> v53.with(() -> (byte)3));
				writer.schema().with("cacheSize", "Cache size in KB").shortNumber(v54 -> v54.with(() -> (short)512));
				writer.schema().with("sessionTimeout", "Session timeout in ms").longNumber(v55 -> v55.with(() -> 3600000L));
				writer.schema().with("cpuThreshold", "CPU usage threshold").floatNumber(v56 -> v56.with(() -> 85.5f));
				writer.schema().with("memoryThreshold", "Memory usage threshold").doubleNumber(v57 -> v57.with(() -> 90.25));
			}
		};

		// Server section (level 1 nesting)
		ConfigPart serverPart = new TestPart(ConfigPath.of("server")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("host", "Server hostname").string(v5 -> v5.with(() -> "0.0.0.0"));
				writer.schema().with("port", "Server listening port", "Must be between 1024 and 65535").intNumber(v6 -> {
					v6.with(() -> 8080);
					v6.validator(obj -> obj instanceof Integer n && n >= 1024 && n <= 65535);
				});
				writer.schema().with("ssl", "Whether SSL is enabled").bool(v7 -> v7.with(() -> true));
				writer.schema().with("allowedOrigins", "CORS allowed origins").list(v25 -> v25.with(() -> List.of("https://example.com", "https://app.example.com")));
			}
		};

		// Server -> network section (level 2 nesting)
		ConfigPart networkPart = new TestPart(ConfigPath.of("server", "network")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("interface", "Network interface name").string(v9 -> v9.with(() -> "eth0"));
				writer.schema().with("timeout", "Connection timeout in milliseconds").intNumber(v10 -> v10.with(() -> 5000));
				writer.schema().with("retries", "Number of retry attempts").intNumber(v11 -> v11.with(() -> 3));
			}
		};

		// Server -> network -> firewall section (level 3 nesting)
		ConfigPart firewallPart = new TestPart(ConfigPath.of("server", "network", "firewall")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("enabled", "Whether firewall is active").bool(v13 -> v13.with(() -> true));
				writer.schema().with("defaultPolicy", "Default firewall policy").string(v14 -> v14.with(() -> "deny"));
			}
		};

		// Database section (level 1 nesting)
		ConfigPart dbPart = new TestPart(ConfigPath.of("database")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("driver", "JDBC driver class").string(v27 -> v27.with(() -> "com.mysql.cj.jdbc.Driver"));
				writer.schema().with("url", "Database connection URL").string(v28 -> v28.with(() -> "jdbc:mysql://localhost:3306/mydb"));
				writer.schema().with("username", "Database username").string(v29 -> v29.with(() -> "admin"));
				writer.schema().with("password", "Database password").string(v30 -> v30.with(() -> "secret"));
			}
		};

		// Database -> pool section (level 2 nesting)
		ConfigPart poolPart = new TestPart(ConfigPath.of("database", "pool")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("minSize", "Minimum pool size").intNumber(v32 -> v32.with(() -> 5));
				writer.schema().with("maxSize", "Maximum pool size").intNumber(v33 -> v33.with(() -> 20));
				writer.schema().with("idleTimeout", "Idle connection timeout in ms").longNumber(v34 -> v34.with(() -> 60000L));
				writer.schema().with("maxLifetime", "Maximum connection lifetime in ms").longNumber(v35 -> v35.with(() -> 1800000L));
				writer.schema().with("testOnBorrow", "Test connections on borrow").bool(v36 -> v36.with(() -> true));
			}
		};

		// Append all parts before configuring
		registry.append(file, topPart);
		registry.append(file, serverPart);
		registry.append(file, networkPart);
		registry.append(file, firewallPart);
		registry.append(file, dbPart);
		registry.append(file, poolPart);
		registry.configure().join();

		Path filePath = tempDir.resolve("test-complex-nested.yaml");
		assertTrue(Files.exists(filePath), "Config file should be created");

		String content = Files.readString(filePath);

		// Verify top-level scalars
		assertTrue(content.contains("MyApp"), "Should contain appName: " + content);
		assertTrue(content.contains("2.0.0"), "Should contain version: " + content);
		assertTrue(content.contains("production"), "Should contain tag 'production': " + content);
		assertTrue(content.contains("web"), "Should contain tag 'web': " + content);
		assertTrue(content.contains("api"), "Should contain tag 'api': " + content);

		// Verify deep nesting: server -> network -> interface
		assertTrue(content.contains("server"), "Should contain server section: " + content);
		assertTrue(content.contains("network"), "Should contain network section: " + content);
		assertTrue(content.contains("eth0"), "Should contain interface value: " + content);
		assertTrue(content.contains("0.0.0.0"), "Should contain host value: " + content);
		assertTrue(content.contains("8080"), "Should contain port value: " + content);

		// Verify deeper nesting: server -> network -> firewall
		assertTrue(content.contains("firewall"), "Should contain firewall section: " + content);
		assertTrue(content.contains("deny"), "Should contain defaultPolicy: " + content);

		// Verify server-level string list
		assertTrue(content.contains("allowedOrigins"), "Should contain allowedOrigins key: " + content);
		assertTrue(content.contains("https://example.com"), "Should contain CORS origin: " + content);

		// Verify database section
		assertTrue(content.contains("database"), "Should contain database section: " + content);
		assertTrue(content.contains("com.mysql.cj.jdbc.Driver"), "Should contain JDBC driver: " + content);
		assertTrue(content.contains("jdbc:mysql"), "Should contain JDBC URL: " + content);
		assertTrue(content.contains("admin"), "Should contain username: " + content);

		// Verify database -> pool nested section
		assertTrue(content.contains("pool"), "Should contain pool section: " + content);
		assertTrue(content.contains("minSize"), "Should contain minSize: " + content);
		assertTrue(content.contains("maxSize"), "Should contain maxSize: " + content);
		assertTrue(content.contains("60000"), "Should contain idleTimeout: " + content);
		assertTrue(content.contains("1800000"), "Should contain maxLifetime: " + content);

		// Verify numeric types
		assertTrue(content.contains("2048"), "Should contain maxMemory: " + content);
		assertTrue(content.contains("512"), "Should contain cacheSize: " + content);
		assertTrue(content.contains("3600000"), "Should contain sessionTimeout: " + content);
		assertTrue(content.contains("85.5"), "Should contain cpuThreshold: " + content);
		assertTrue(content.contains("90.25"), "Should contain memoryThreshold: " + content);

		// Verify comments are present
		assertTrue(content.contains("# The application display name"), "Should contain appName comment: " + content);
		assertTrue(content.contains("# Server hostname"), "Should contain host comment: " + content);
		assertTrue(content.contains("# Network interface name"), "Should contain interface comment: " + content);
		assertTrue(content.contains("# Default firewall policy"), "Should contain firewall policy comment: " + content);
		assertTrue(content.contains("# JDBC driver class"), "Should contain driver comment: " + content);
		assertTrue(content.contains("# Connection timeout in milliseconds"), "Should contain timeout comment: " + content);
		assertTrue(content.contains("# Application tags"), "Should contain tags comment: " + content);
		assertTrue(content.contains("# CORS allowed origins"), "Should contain allowedOrigins comment: " + content);
		assertTrue(content.contains("# Minimum pool size"), "Should contain pool minSize comment: " + content);
		assertTrue(content.contains("# Maximum connection lifetime"), "Should contain pool maxLifetime comment: " + content);
	}

	@Test
	void configure_usingSectionAPI() throws IOException {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-section-api.yaml"));

		// Single part using the section() API for all nesting
		ConfigPart part = new TestPart() {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				// Top-level scalar
				writer.schema().with("appName", "Application name").string(v -> v.with(() -> "SectionApp"));

				// Top-level string list
				writer.schema().with("tags", "Application tags").list(v -> v.with(() -> List.of("web", "api")));

				// Level 1: server section with nested subsections
				writer.schema().with("server", "Server configuration").section((supplier, value) -> {
					SchemaWriter server = supplier.get();

					server.with("host", "Server hostname").string(v -> v.with(() -> "0.0.0.0"));
					server.with("port", "Server port", "Must be valid").intNumber(v -> {
						v.with(() -> 9090);
						v.validator(obj -> obj instanceof Integer n && n > 0 && n <= 65535);
					});
					server.with("ssl", "Enable SSL").bool(v -> v.with(() -> false));

					// Level 2: server -> network section
					server.with("network", "Network settings").section((s2, v2) -> {
						SchemaWriter network = s2.get();
						network.with("interface", "Network interface").string(v3 -> v3.with(() -> "eth0"));
						network.with("timeout", "Connection timeout ms").intNumber(v4 -> v4.with(() -> 3000));
						network.with("retries", "Retry count").byteNumber(v5 -> v5.with(() -> (byte)3));

						// Level 3: server -> network -> firewall section
						network.with("firewall", "Firewall rules").section((s3, v3) -> {
							SchemaWriter firewall = s3.get();
							firewall.with("enabled", "Firewall enabled").bool(v6 -> v6.with(() -> true));
							firewall.with("policy", "Default policy").string(v7 -> v7.with(() -> "allow"));
							v3.with(() -> firewall);
						});
						v2.with(() -> network);
					});

					// Level 2: server -> allowedOrigins (string list inside section)
					server.with("allowedOrigins", "CORS origins").list(v8 -> v8.with(() -> List.of("https://example.com")));

					value.with(() -> server);
				});

				// Level 1: database section with nested pool subsection
				writer.schema().with("database", "Database configuration").section((supplier, value) -> {
					SchemaWriter db = supplier.get();

					db.with("url", "JDBC URL").string(v -> v.with(() -> "jdbc:postgresql://localhost:5432/db"));
					db.with("username", "DB username").string(v -> v.with(() -> "dbadmin"));

					// Level 2: database -> pool section
					db.with("pool", "Connection pool").section((s2, v2) -> {
						SchemaWriter pool = s2.get();
						pool.with("minSize", "Min pool size").shortNumber(v3 -> v3.with(() -> (short)2));
						pool.with("maxSize", "Max pool size").intNumber(v4 -> v4.with(() -> 10));
						pool.with("idleTimeout", "Idle timeout ms").longNumber(v5 -> v5.with(() -> 60000L));
						pool.with("maxLifetime", "Max lifetime ms").longNumber(v6 -> v6.with(() -> 1800000L));
						pool.with("testOnBorrow", "Test on borrow").bool(v7 -> v7.with(() -> true));
						pool.with("acquireIncrement", "Acquire increment").floatNumber(v8 -> v8.with(() -> 1.5f));
						pool.with("connectionTimeout", "Connection timeout").doubleNumber(v9 -> v9.with(() -> 5.25));
						v2.with(() -> pool);
					});

					value.with(() -> db);
				});

				// Level 1: plugins section using sections() API (list of mappings)
				writer.schema().with("plugins", "Plugin configurations").sections((supplier, value) -> {
					List<SchemaWriter> plugins = new ArrayList<>();

					// Plugin 1
					SchemaWriter p1 = supplier.get();
					p1.with("name", "Plugin name").string(v -> v.with(() -> "AuthPlugin"));
					p1.with("enabled", "Plugin enabled").bool(v -> v.with(() -> true));
					p1.with("priority", "Plugin priority").intNumber(v -> v.with(() -> 1));
					plugins.add(p1);

					// Plugin 2
					SchemaWriter p2 = supplier.get();
					p2.with("name", "Plugin name").string(v -> v.with(() -> "CachePlugin"));
					p2.with("enabled", "Plugin enabled").bool(v -> v.with(() -> false));
					p2.with("priority", "Plugin priority").intNumber(v -> v.with(() -> 2));
					plugins.add(p2);

					value.with(() -> plugins);
				});
			}
		};

		registry.append(file, part);
		registry.configure().join();

		Path filePath = tempDir.resolve("test-section-api.yaml");
		assertTrue(Files.exists(filePath), "Config file should be created");

		String content = Files.readString(filePath);

		// Verify top-level scalars
		assertTrue(content.contains("SectionApp"), "Should contain appName: " + content);
		assertTrue(content.contains("web"), "Should contain tag 'web': " + content);
		assertTrue(content.contains("api"), "Should contain tag 'api': " + content);

		// Verify server section (level 1)
		assertTrue(content.contains("server"), "Should contain server section: " + content);
		assertTrue(content.contains("0.0.0.0"), "Should contain host: " + content);
		assertTrue(content.contains("9090"), "Should contain port: " + content);

		// Verify server -> network section (level 2)
		assertTrue(content.contains("network"), "Should contain network section: " + content);
		assertTrue(content.contains("eth0"), "Should contain interface: " + content);
		assertTrue(content.contains("3000"), "Should contain timeout: " + content);

		// Verify server -> network -> firewall section (level 3)
		assertTrue(content.contains("firewall"), "Should contain firewall section: " + content);
		assertTrue(content.contains("allow"), "Should contain policy: " + content);

		// Verify server -> allowedOrigins (string list inside section)
		assertTrue(content.contains("allowedOrigins"), "Should contain allowedOrigins: " + content);
		assertTrue(content.contains("https://example.com"), "Should contain CORS origin: " + content);

		// Verify database section (level 1)
		assertTrue(content.contains("database"), "Should contain database section: " + content);
		assertTrue(content.contains("jdbc:postgresql"), "Should contain JDBC URL: " + content);
		assertTrue(content.contains("dbadmin"), "Should contain username: " + content);

		// Verify database -> pool section (level 2)
		assertTrue(content.contains("pool"), "Should contain pool section: " + content);
		assertTrue(content.contains("minSize"), "Should contain minSize: " + content);
		assertTrue(content.contains("maxSize"), "Should contain maxSize: " + content);
		assertTrue(content.contains("60000"), "Should contain idleTimeout: " + content);
		assertTrue(content.contains("1800000"), "Should contain maxLifetime: " + content);
		assertTrue(content.contains("1.5"), "Should contain acquireIncrement: " + content);
		assertTrue(content.contains("5.25"), "Should contain connectionTimeout: " + content);

		// Verify plugins sections (list of mappings)
		assertTrue(content.contains("plugins"), "Should contain plugins key: " + content);
		assertTrue(content.contains("AuthPlugin"), "Should contain AuthPlugin: " + content);
		assertTrue(content.contains("CachePlugin"), "Should contain CachePlugin: " + content);

		// Verify comments are present
		assertTrue(content.contains("# Application name"), "Should contain appName comment: " + content);
		assertTrue(content.contains("# Server configuration"), "Should contain server comment: " + content);
		assertTrue(content.contains("# Server hostname"), "Should contain host comment: " + content);
		assertTrue(content.contains("# Network settings"), "Should contain network comment: " + content);
		assertTrue(content.contains("# Firewall rules"), "Should contain firewall comment: " + content);
		assertTrue(content.contains("# Connection pool"), "Should contain pool comment: " + content);
		assertTrue(content.contains("# Plugin configurations"), "Should contain plugins comment: " + content);
		assertTrue(content.contains("# Min pool size"), "Should contain minSize comment: " + content);
		assertTrue(content.contains("# Max lifetime"), "Should contain maxLifetime comment: " + content);
	}

	@Test
	void configure_multipleRegenerations_noDuplicateBlankLines() throws IOException {
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-blank-lines.yaml"));
		ConfigPart part = new TestPart() {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("name", "Display name").string(v -> v.with(() -> "TestApp"));
				writer.schema().with("port", "Server port").intNumber(v -> v.with(() -> 8080));
				writer.schema().with("enabled", "Feature toggle").bool(v -> v.with(() -> true));
			}
		};

		// Generate the file 3 times
		for (int i = 0; i < 3; i++) {
			ConfigurationRegistry reg = new ConfigurationRegistry();
			reg.append(file, part);
			reg.configure().join();
		}

		String content = Files.readString(tempDir.resolve("test-blank-lines.yaml"));

		// Count consecutive blank lines — should never have more than 1 blank line between keys
		String[] lines = content.split("\n");
		int consecutiveBlanks = 0;
		int maxConsecutiveBlanks = 0;
		for (String line : lines) {
			if (line.trim().isEmpty()) {
				consecutiveBlanks++;
				maxConsecutiveBlanks = Math.max(maxConsecutiveBlanks, consecutiveBlanks);
			} else {
				consecutiveBlanks = 0;
			}
		}
		// Should have at most 1 blank line between siblings, not 2+ from accumulation
		assertTrue(maxConsecutiveBlanks <= 1, "Should not have duplicate blank lines (max consecutive: " + maxConsecutiveBlanks + "): " + content);

		// Verify content is still correct
		assertTrue(content.contains("TestApp"), "Should contain name: " + content);
		assertTrue(content.contains("8080"), "Should contain port: " + content);
		assertTrue(content.contains("true"), "Should contain enabled: " + content);
	}

	@Test
	void configure_emptyRegistry_doesNothing() {
		// No parts appended — configure should complete without error
		registry.configure().join();
		// No assertion needed — no exception means success
	}

	@Test
	void configure_partCanReadExistingConfig() throws IOException {
		// Pre-create config
		Path configPath = tempDir.resolve("test-reader.yaml");
		Files.writeString(configPath, "name: 'preExisting'\n");

		ConfigFile file = ConfigFile.absolute(configPath);
		// Part reads existing value via reader and writes a derived value
		ConfigPart part = new TestPart() {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				// The reader is passed at construction — in real usage the registry
				// creates the writer, but init() receives it
				writer.schema().with("derived", "Value derived from existing configuration").string(v -> v.with(() -> "derived-from-config"));
			}
		};

		registry.append(file, part);
		registry.configure().join();

		String content = Files.readString(configPath);
		assertTrue(content.contains("preExisting"), "Original value preserved: " + content);
		assertTrue(content.contains("derived-from-config"), "New derived value added: " + content);
		assertTrue(content.contains("# Value derived from existing"), "Should contain derived comment: " + content);
	}

	// --- Same type merge tests ---

	@Test
	void append_samePath_sameType_otherValidatorOnly() throws IOException {
		// Two parts with the same path, each defining a validator on the same key.
		// The other part's value completely replaces the current part's value.
		// Only part2's validator (n <= 65535) applies.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-merged-validators.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server port", "Must be positive").intNumber(v -> {
					v.with(() -> 8080);
					v.validator(obj -> obj instanceof Integer n && n > 0);
				});
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server port", "Must be <= 65535").intNumber(v -> {
					v.with(() -> 8080);
					v.validator(obj -> obj instanceof Integer n && n <= 65535);
				});
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		// Default value 8080 passes part2's validator (8080 <= 65535)
		Path filePath = tempDir.resolve("test-merged-validators.yaml");
		String content = Files.readString(filePath);
		assertTrue(content.contains("8080"), "Valid port should be preserved: " + content);
	}

	@Test
	void append_samePath_sameType_otherValueReplaces() throws IOException {
		// When two parts define the same key with the same type, the other part's value
		// completely replaces the current part's value (including validator).
		// Only part2's validator (n <= 65535) applies — part1's validator is discarded.
		// The value -1 passes part2's validator, so it is preserved.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-merged-validators-invalid.yaml"));
		Files.writeString(file.path(), "port: -1\n");

		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server port", "Must be positive").intNumber(v -> {
					v.with(() -> 8080);
					v.validator(obj -> obj instanceof Integer n && n > 0);
				});
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server port", "Must be <= 65535").intNumber(v -> {
					v.with(() -> 8080);
					v.validator(obj -> obj instanceof Integer n && n <= 65535);
				});
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		// Only part2's validator applies (-1 <= 65535 is true), so -1 is preserved
		String content = Files.readString(file.path());
		assertTrue(content.contains("-1"), "Value passing other validator should be preserved: " + content);
	}

	@Test
	void append_samePath_sameType_otherValidatorRejects_replaced() throws IOException {
		// Pre-create a config with a value that fails part2's validator.
		// Only part2's validator (n <= 65535) applies — part1's validator is discarded.
		// The value 70000 fails "<= 65535", so it should be replaced.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-merged-validators-second-fails.yaml"));
		Files.writeString(file.path(), "port: 70000\n");

		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server port", "Must be positive").intNumber(v -> {
					v.with(() -> 8080);
					v.validator(obj -> obj instanceof Integer n && n > 0);
				});
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server port", "Must be <= 65535").intNumber(v -> {
					v.with(() -> 8080);
					v.validator(obj -> obj instanceof Integer n && n <= 65535);
				});
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		// 70000 fails part2's validator (<= 65535), so it should be replaced with the default
		String content = Files.readString(file.path());
		assertTrue(content.contains("8080"), "Invalid port should be replaced with default: " + content);
		assertFalse(content.contains("70000"), "Invalid value should be removed: " + content);
	}

	// --- Complex merge tests ---

	@Test
	void append_samePath_complexMerge_nestedSections_merged() throws IOException {
		// Two parts with the same path, each defining nested sections.
		// The nested schemas should be recursively merged.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-complex-nested-merge.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.of("server")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("host", "Server hostname").string(v -> v.with(() -> "localhost"));
				writer.schema().with("network", "Network settings").section((supplier, value) -> {
					SchemaWriter network = supplier.get();
					network.with("interface", "Network interface").string(v2 -> v2.with(() -> "eth0"));
					network.with("timeout", "Connection timeout").intNumber(v3 -> v3.with(() -> 5000));
					value.with(() -> network);
				});
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.of("server")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server port").intNumber(v -> v.with(() -> 9090));
				writer.schema().with("network", "Network settings").section((supplier, value) -> {
					SchemaWriter network = supplier.get();
					network.with("retries", "Retry count").intNumber(v2 -> v2.with(() -> 3));
					value.with(() -> network);
				});
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		Path filePath = tempDir.resolve("test-complex-nested-merge.yaml");
		String content = Files.readString(filePath);

		// Verify server section contains values from both parts
		assertTrue(content.contains("server"), "Should contain server section: " + content);
		assertTrue(content.contains("localhost"), "Should contain host from part1: " + content);
		assertTrue(content.contains("9090"), "Should contain port from part2: " + content);

		// Verify nested network section is merged with values from both parts
		assertTrue(content.contains("network"), "Should contain network section: " + content);
		assertTrue(content.contains("eth0"), "Should contain interface from part1: " + content);
		assertTrue(content.contains("5000"), "Should contain timeout from part1: " + content);
		assertTrue(content.contains("3"), "Should contain retries from part2: " + content);
	}

	@Test
	void append_samePath_complexMerge_stringWithStringList() throws IOException {
		// Two parts with the same path: one defines a STRING, the other a STRING_LIST.
		// The merge should combine them into a list.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-complex-string-list-merge.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("tags", "Application tags").list(v -> v.with(() -> List.of("alpha", "beta")));
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("tags", "Application tags").list(v -> v.with(() -> List.of("gamma", "delta")));
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		Path filePath = tempDir.resolve("test-complex-string-list-merge.yaml");
		String content = Files.readString(filePath);

		// Both lists should be combined
		assertTrue(content.contains("alpha"), "Should contain alpha from part1: " + content);
		assertTrue(content.contains("beta"), "Should contain beta from part1: " + content);
		assertTrue(content.contains("gamma"), "Should contain gamma from part2: " + content);
		assertTrue(content.contains("delta"), "Should contain delta from part2: " + content);
	}

	@Test
	void append_samePath_complexMerge_sectionsList_merged() throws IOException {
		// Two parts with the same path, each defining a sections() list (MAPPING_LIST).
		// The merge should combine both lists.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-complex-sections-list-merge.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("plugins", "Plugin configurations").sections((supplier, value) -> {
					List<SchemaWriter> plugins = new ArrayList<>();
					SchemaWriter p1 = supplier.get();
					p1.with("name", "Plugin name").string(v -> v.with(() -> "AuthPlugin"));
					p1.with("enabled", "Plugin enabled").bool(v -> v.with(() -> true));
					plugins.add(p1);
					value.with(() -> plugins);
				});
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("plugins", "Plugin configurations").sections((supplier, value) -> {
					List<SchemaWriter> plugins = new ArrayList<>();
					SchemaWriter p2 = supplier.get();
					p2.with("name", "Plugin name").string(v -> v.with(() -> "CachePlugin"));
					p2.with("enabled", "Plugin enabled").bool(v -> v.with(() -> false));
					plugins.add(p2);
					value.with(() -> plugins);
				});
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		Path filePath = tempDir.resolve("test-complex-sections-list-merge.yaml");
		String content = Files.readString(filePath);

		// Both plugin lists should be combined
		assertTrue(content.contains("plugins"), "Should contain plugins key: " + content);
		assertTrue(content.contains("AuthPlugin"), "Should contain AuthPlugin from part1: " + content);
		assertTrue(content.contains("CachePlugin"), "Should contain CachePlugin from part2: " + content);
	}

	@Test
	void append_samePath_complexMerge_constantReplaces() throws IOException {
		// Two parts with the same path: part2 defines a constant value for a key
		// that part1 also defines. The constant should completely replace part1's value.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-complex-constant-replace.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("version", "Plugin version").string(v -> v.with(() -> "0.0.1"));
				writer.schema().with("name", "Plugin name").string(v -> v.with(() -> "MyPlugin"));
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("version", "Plugin version", "Do not modify").string(v -> v.constant("2.0.0"));
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		Path filePath = tempDir.resolve("test-complex-constant-replace.yaml");
		String content = Files.readString(filePath);

		// Constant from part2 should replace part1's value
		assertTrue(content.contains("2.0.0"), "Constant version should replace default: " + content);
		assertFalse(content.contains("0.0.1"), "Original default should not appear: " + content);

		// Non-overlapping key from part1 should still be present
		assertTrue(content.contains("MyPlugin"), "Non-overlapping name should be preserved: " + content);
	}

	@Test
	void append_samePath_threeWayMerge_allPartsCombined() throws IOException {
		// Three parts with the same path, each defining different keys.
		// All three should be merged into a single part.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-three-way-merge.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("name", "Plugin name").string(v -> v.with(() -> "ThreeWayPlugin"));
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server port").intNumber(v -> v.with(() -> 8080));
			}
		};
		ConfigPart part3 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("enabled", "Feature enabled").bool(v -> v.with(() -> true));
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.append(file, part3);

		// All three parts should be merged into one
		ConfigPart[] parts = registry.parts(file);
		assertEquals(1, parts.length, "Three parts should be merged into one");

		registry.configure().join();

		Path filePath = tempDir.resolve("test-three-way-merge.yaml");
		String content = Files.readString(filePath);

		// All values from all three parts should be present
		assertTrue(content.contains("ThreeWayPlugin"), "Should contain name from part1: " + content);
		assertTrue(content.contains("8080"), "Should contain port from part2: " + content);
		assertTrue(content.contains("true"), "Should contain enabled from part3: " + content);
	}

	@Test
	void append_samePath_threeWayMerge_overlappingKeys_lastWins() throws IOException {
		// Three parts with the same path, all defining the same key.
		// The last part's value should win.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-three-way-overlap.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("name", "Plugin name").string(v -> v.with(() -> "first"));
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("name", "Plugin name").string(v -> v.with(() -> "second"));
			}
		};
		ConfigPart part3 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("name", "Plugin name").string(v -> v.with(() -> "third"));
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.append(file, part3);
		registry.configure().join();

		Path filePath = tempDir.resolve("test-three-way-overlap.yaml");
		String content = Files.readString(filePath);

		// Last part's value should win
		assertTrue(content.contains("third"), "Last part's value should win: " + content);
		assertFalse(content.contains("first"), "First part's value should not appear: " + content);
		assertFalse(content.contains("second"), "Second part's value should not appear: " + content);
	}

	// --- Validator non-merging tests ---
	// These tests verify that merging validators never happens.
	// The other schema's validator should always be the one that applies.

	@Test
	void append_samePath_stringToStringList_listValidatorOnly() throws IOException {
		// Two parts with the same path: part1 defines a STRING, part2 defines a STRING_LIST.
		// The merge combines them into a list, but only part2's (the list's) validator applies.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-string-to-list-validator.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("tag", "Single tag").string(v -> {
					v.with(() -> "alpha");
					v.validator(obj -> obj instanceof String s && s.startsWith("a"));
				});
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("tag", "Tag list").list(v -> {
					v.with(() -> List.of("beta", "gamma"));
					v.validator(obj -> obj instanceof List<?> list && !list.isEmpty());
				});
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		Path filePath = tempDir.resolve("test-string-to-list-validator.yaml");
		String content = Files.readString(filePath);

		// The merged list should contain values from both parts
		assertTrue(content.contains("alpha"), "Should contain alpha from part1: " + content);
		assertTrue(content.contains("beta"), "Should contain beta from part2: " + content);
		assertTrue(content.contains("gamma"), "Should contain gamma from part2: " + content);
	}

	@Test
	void append_samePath_stringListWithStringList_otherValidatorOnly() throws IOException {
		// Two parts with the same path, both defining STRING_LIST with different validators.
		// Only part2's (other's) validator should apply, not merged.
		// Pre-create a config with a value that would fail part1's validator but pass part2's.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-list-list-validator.yaml"));
		Files.writeString(file.path(), "tags:\n  - zeta\n");

		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("tags", "Tag list", "Must start with a").list(v -> {
					v.with(() -> List.of("alpha"));
					v.validator(obj -> obj instanceof List<?> list && list.stream().allMatch(item -> item instanceof String s && s.startsWith("a")));
				});
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("tags", "Tag list", "Must not be empty").list(v -> {
					v.with(() -> List.of("beta"));
					v.validator(obj -> obj instanceof List<?> list && !list.isEmpty());
				});
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		// "zeta" fails part1's validator (doesn't start with "a") but passes part2's (non-empty).
		// Only part2's validator applies, so "zeta" should be preserved.
		String content = Files.readString(file.path());
		assertTrue(content.contains("zeta"), "Value passing other validator should be preserved: " + content);
	}

	@Test
	void append_samePath_mappingToMappingList_listValidatorOnly() throws IOException {
		// Two parts with the same path: part1 defines a MAPPING (section), part2 defines a MAPPING_LIST (sections).
		// The merge combines them, but only part2's (the list's) validator applies.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-mapping-to-list-validator.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("plugin", "Single plugin").section((supplier, valueWriter) -> {
					SchemaWriter p = supplier.get();
					p.with("name", "Plugin name").string(v -> v.with(() -> "SinglePlugin"));
					valueWriter.with(() -> p);
					valueWriter.validator(_ -> true); // part1's validator — should NOT apply
				});
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("plugin", "Plugin list").sections((supplier, valueWriter) -> {
					List<SchemaWriter> plugins = new ArrayList<>();
					SchemaWriter p2 = supplier.get();
					p2.with("name", "Plugin name").string(v -> v.with(() -> "ListPlugin"));
					plugins.add(p2);
					valueWriter.with(() -> plugins);
				});
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		Path filePath = tempDir.resolve("test-mapping-to-list-validator.yaml");
		String content = Files.readString(filePath);

		// Both plugins should appear in the merged list
		assertTrue(content.contains("SinglePlugin"), "Should contain SinglePlugin from part1: " + content);
		assertTrue(content.contains("ListPlugin"), "Should contain ListPlugin from part2: " + content);
	}

	@Test
	void append_samePath_mappingListToMapping_otherValidatorOnly() throws IOException {
		// Two parts with the same path: part1 defines a MAPPING_LIST (sections), part2 defines a MAPPING (section).
		// The merge combines them, but only part2's (the other's) validator applies.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-list-to-mapping-validator.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("plugin", "Plugin list").sections((supplier, valueWriter) -> {
					List<SchemaWriter> plugins = new ArrayList<>();
					SchemaWriter p1 = supplier.get();
					p1.with("name", "Plugin name").string(v -> v.with(() -> "ListPlugin"));
					plugins.add(p1);
					valueWriter.with(() -> plugins);
					valueWriter.validator(obj -> obj instanceof List<?> list && list.size() > 5); // part1's validator — should NOT apply
				});
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.empty()) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("plugin", "Single plugin").section((supplier, valueWriter) -> {
					SchemaWriter p = supplier.get();
					p.with("name", "Plugin name").string(v -> v.with(() -> "SinglePlugin"));
					valueWriter.with(() -> p);
				});
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		Path filePath = tempDir.resolve("test-list-to-mapping-validator.yaml");
		String content = Files.readString(filePath);

		// Both plugins should appear in the merged result
		assertTrue(content.contains("ListPlugin"), "Should contain ListPlugin from part1: " + content);
		assertTrue(content.contains("SinglePlugin"), "Should contain SinglePlugin from part2: " + content);
	}

	@Test
	void append_samePath_mappingWithMapping_otherValidatorOnly() throws IOException {
		// Two parts with the same path, both defining a MAPPING (section) with validators.
		// Only part2's (other's) validator should apply, not merged.
		// Pre-create a config with a value that would fail part1's validator but pass part2's.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-mapping-mapping-validator.yaml"));
		Files.writeString(file.path(), "server:\n  port: 70000\n");

		ConfigPart part1 = new TestPart(ConfigPath.of("server")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server port", "Must be positive").intNumber(v -> {
					v.with(() -> 8080);
					v.validator(obj -> obj instanceof Integer n && n > 0);
				});
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.of("server")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server port", "Must be <= 100000").intNumber(v -> {
					v.with(() -> 8080);
					v.validator(obj -> obj instanceof Integer n && n <= 100000);
				});
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		// 70000 fails part1's validator (n > 0 is true, but this is same-type overlap so other replaces entirely).
		// Only part2's validator applies (70000 <= 100000 is true), so 70000 should be preserved.
		String content = Files.readString(file.path());
		assertTrue(content.contains("70000"), "Value passing other validator should be preserved: " + content);
	}

	@Test
	void append_samePath_complexMerge_validatorsWithNestedSections() throws IOException {
		// Two parts with the same path, each defining validators on nested section keys.
		// The merged validators should apply to the nested values.
		ConfigFile file = ConfigFile.absolute(tempDir.resolve("test-validators-nested-merge.yaml"));
		ConfigPart part1 = new TestPart(ConfigPath.of("server")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server port", "Must be positive").intNumber(v -> {
					v.with(() -> 8080);
					v.validator(obj -> obj instanceof Integer n && n > 0);
				});
			}
		};
		ConfigPart part2 = new TestPart(ConfigPath.of("server")) {
			@Override
			public void init(@NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
				writer.schema().with("port", "Server port", "Must be <= 65535").intNumber(v -> {
					v.with(() -> 8080);
					v.validator(obj -> obj instanceof Integer n && n <= 65535);
				});
			}
		};

		registry.append(file, part1);
		registry.append(file, part2);
		registry.configure().join();

		// A valid value within both ranges should be preserved
		Path filePath = tempDir.resolve("test-validators-nested-merge.yaml");
		String content = Files.readString(filePath);
		assertTrue(content.contains("server"), "Should contain server section: " + content);
		assertTrue(content.contains("8080"), "Valid port should be preserved: " + content);
	}

	// --- Helper classes ---

	private static class TestPart extends ConfigPart {

		private final ConfigPath path;
		protected ConfigReader reader;

		TestPart() {
			this.path = ConfigPath.empty();
		}

		TestPart(ConfigPath path) {
			this.path = path;
		}

		@Override
		public ConfigPath path() {
			return path;
		}

		@Override
		public void init(final @NonNull ConfigReader reader, @NonNull ConfigWriter writer) {
			this.reader = reader;
			// Default: do nothing
		}
	}
}
