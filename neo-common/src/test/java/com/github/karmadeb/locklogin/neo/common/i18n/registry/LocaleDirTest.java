package com.github.karmadeb.locklogin.neo.common.i18n.registry;

import com.github.karmadeb.locklogin.neo.api.i18n.registry.LocaleDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LocaleDirTest {

	private Path tempDir;

	@BeforeEach
	void setUp() throws IOException {
		tempDir = Files.createTempDirectory("localeDirTest");
	}

	// --- Constructor tests ---

	@Test
	void constructor_emptyRoute_createsDefaultPath() {
		LocaleDir dir = new LocaleDir();

		assertTrue(dir.path().toString().contains("LockLogin"));
		assertTrue(dir.path().toString().contains("i18n"));
	}

	@Test
	void constructor_withRoute_createsNestedPath() {
		LocaleDir dir = new LocaleDir("plugin", "sub");

		assertTrue(dir.path().toString().contains("LockLogin"));
		assertTrue(dir.path().toString().contains("i18n"));
		assertTrue(dir.path().toString().contains("plugin"));
		assertTrue(dir.path().toString().contains("sub"));
	}

	// --- path() tests ---

	@Test
	void path_returnsPath() {
		LocaleDir dir = new LocaleDir("test");

		assertNotNull(dir.path());
		assertTrue(dir.path().toString().contains("test"));
	}

	// --- read() tests ---

	@Test
	void read_createsDirectoryIfNotExists() throws IOException {
		Path newDir = tempDir.resolve("new").resolve("sub");
		LocaleDir dir = LocaleDir.absolute(newDir);

		assertFalse(Files.exists(newDir));
		try (var stream = dir.read(Locale.ENGLISH)) {
			// Stream should open successfully
			assertNotNull(stream);
		}
		assertTrue(Files.exists(newDir));
	}

	@Test
	void read_createsFileIfNotExists() throws IOException {
		LocaleDir dir = LocaleDir.absolute(tempDir);

		Path file = tempDir.resolve("localization.en.yml");
		assertFalse(Files.exists(file));

		try (var stream = dir.read(Locale.ENGLISH)) {
			assertNotNull(stream);
		}
		assertTrue(Files.exists(file));
	}

	@Test
	void read_returnsInputStream() throws IOException {
		LocaleDir dir = LocaleDir.absolute(tempDir);

		try (var stream = dir.read(Locale.ENGLISH)) {
			assertNotNull(stream);
		}
	}

	@Test
	void read_nullLocale_throwsNullPointerException() {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> dir.read(null));
	}

	// --- write() tests ---

	@Test
	void write_createsDirectoryIfNotExists() throws IOException {
		Path newDir = tempDir.resolve("new").resolve("sub");
		LocaleDir dir = LocaleDir.absolute(newDir);

		assertFalse(Files.exists(newDir));
		try (var stream = dir.write(Locale.ENGLISH)) {
			assertNotNull(stream);
		}
		assertTrue(Files.exists(newDir));
	}

	@Test
	void write_createsFileIfNotExists() throws IOException {
		LocaleDir dir = LocaleDir.absolute(tempDir);

		Path file = tempDir.resolve("localization.fr.yml");
		assertFalse(Files.exists(file));

		try (var stream = dir.write(Locale.FRENCH)) {
			assertNotNull(stream);
		}
		assertTrue(Files.exists(file));
	}

	@Test
	void write_returnsOutputStream() throws IOException {
		LocaleDir dir = LocaleDir.absolute(tempDir);

		try (var stream = dir.write(Locale.ENGLISH)) {
			assertNotNull(stream);
		}
	}

	@Test
	void write_writesData() throws IOException {
		LocaleDir dir = LocaleDir.absolute(tempDir);

		try (var stream = dir.write(Locale.ENGLISH)) {
			stream.write("test data".getBytes());
		}

		Path file = tempDir.resolve("localization.en.yml");
		String content = Files.readString(file);
		assertEquals("test data", content);
	}

	@Test
	void write_nullLocale_throwsNullPointerException() {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		// noinspection DataFlowIssue
		assertThrows(NullPointerException.class, () -> dir.write(null));
	}

	@Test
	void write_fileName_format() throws IOException {
		LocaleDir dir = LocaleDir.absolute(tempDir);

		try (var stream = dir.write(Locale.of("es", "ES"))) {
			// Just open and close
		}

		// Should create localization.es_ES.yml
		assertTrue(Files.exists(tempDir.resolve("localization.es_ES.yml")));
	}

	// --- equals() tests ---

	@Test
	void equals_samePath_returnsTrue() {
		LocaleDir dir1 = LocaleDir.absolute(tempDir);
		LocaleDir dir2 = LocaleDir.absolute(tempDir);

		assertEquals(dir1, dir2);
	}

	@Test
	void equals_differentPath_returnsFalse() throws IOException {
		Path otherDir = Files.createTempDirectory("localeDirTest_other");
		LocaleDir dir1 = LocaleDir.absolute(tempDir);
		LocaleDir dir2 = LocaleDir.absolute(otherDir);

		assertNotEquals(dir1, dir2);
	}

	@Test
	void equals_sameInstance_returnsTrue() {
		LocaleDir dir = new LocaleDir("test");
		assertEquals(dir, dir);
	}

	@Test
	void equals_null_returnsFalse() {
		LocaleDir dir = new LocaleDir("test");
		assertNotEquals(dir, null);
	}

	@Test
	void equals_differentType_returnsFalse() {
		LocaleDir dir = new LocaleDir("test");
		assertNotEquals(dir, "string");
	}

	// --- hashCode() tests ---

	@Test
	void hashCode_samePath_sameHashCode() {
		LocaleDir dir1 = LocaleDir.absolute(tempDir);
		LocaleDir dir2 = LocaleDir.absolute(tempDir);

		assertEquals(dir1.hashCode(), dir2.hashCode());
	}

	@Test
	void hashCode_consistent() {
		LocaleDir dir = new LocaleDir("test");

		int hash1 = dir.hashCode();
		int hash2 = dir.hashCode();
		assertEquals(hash1, hash2);
	}

	// --- toString() tests ---

	@Test
	void toString_returnsAbsolutePath() {
		LocaleDir dir = LocaleDir.absolute(tempDir);

		String str = dir.toString();
		assertTrue(str.contains(tempDir.toString()));
	}

	@Test
	void toString_relativePath_returnsAbsolute() {
		LocaleDir dir = new LocaleDir("test");

		String str = dir.toString();
		// Should be absolute
		assertTrue(str.startsWith("/") || str.matches("^[A-Z]:\\\\.*"));
	}

	// --- absolute() tests ---

	@Test
	void absolute_createsLocaleDir() {
		LocaleDir dir = LocaleDir.absolute(tempDir);
		assertNotNull(dir);
		assertEquals(tempDir, dir.path());
	}

	@Test
	void absolute_nullPath_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> LocaleDir.absolute(null));
	}

	@Test
	void absolute_relativePath_throwsIllegalArgumentException() {
		Path relative = Path.of("relative", "path");
		assertThrows(IllegalArgumentException.class, () -> LocaleDir.absolute(relative));
	}

	// --- Constructor with varargs ---

	@Test
	void constructor_singleSegment() {
		LocaleDir dir = new LocaleDir("plugin");
		assertTrue(dir.path().toString().contains("plugin"));
	}

	@Test
	void constructor_multipleSegments() {
		LocaleDir dir = new LocaleDir("a", "b", "c");
		String path = dir.path().toString();
		assertTrue(path.contains("a"));
		assertTrue(path.contains("b"));
		assertTrue(path.contains("c"));
	}
}
