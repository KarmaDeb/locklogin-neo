package com.github.karmadeb.locklogin.neo.api.i18n.registry;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents a localization directory. Does not allow
 * by himself to read, nor save the file, but it helps
 * to it
 */
public final class LocaleDir {

	public static final LocaleDir MESSAGES = new LocaleDir("messages");
	private final Path path;

	/**
	 * Create a new localization directory
	 * @param route the localization directory route
	 */
	public LocaleDir(final String... route) {
		this.path = LocaleDir.resolvePath(route);
	}

	/**
	 * Create a new localization directory
	 * @param path the directory path
	 */
	@Contract("null -> fail")
	private LocaleDir(final Path path) {
		this.path = Objects.requireNonNull(path);
	}

	/**
	 * Get the directory path
	 * @return the path
	 */
	@NotNull
	public Path path() {
		return this.path;
	}

	/**
	 * Open a stream to read the file
	 * @param locale the locale to read stream for
	 * @return the stream
	 * @throws IOException if the path fails to open
	 */
	@NotNull
	@Contract("null -> fail")
	public InputStream read(final Locale locale) throws IOException {
		Objects.requireNonNull(locale);

		Path parent = this.path;
		if (!Files.exists(parent)) {
			Files.createDirectories(parent);
		}

		Path file = parent.resolve(String.format("localization.%s.yml", locale));
		if (!Files.exists(file)) {
			Files.createFile(file);
		}

		return Files.newInputStream(file, StandardOpenOption.CREATE);
	}

	/**
	 * Open a stream to write to the file
	 * @param locale the locale to write stream for
	 * @return the stream
	 * @throws IOException if the path fails to open
	 */
	@Contract("null -> fail")
	@NotNull
	public OutputStream write(final Locale locale) throws IOException {
		Objects.requireNonNull(locale);

		Path parent = this.path;
		if (!Files.exists(parent)) {
			Files.createDirectories(parent);
		}

		Path file = parent.resolve(String.format("localization.%s.yml", locale));
		if (!Files.exists(file)) {
			Files.createFile(file);
		}

		return Files.newOutputStream(file, StandardOpenOption.CREATE);
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * <p>
	 * The {@code equals} method implements an <dfn>{@index "equivalence relation"}</dfn>
	 * on non-null object references:
	 * <ul>
	 * <li>It is <i>reflexive</i>: for any non-null reference value
	 *     {@code x}, {@code x.equals(x)} should return
	 *     {@code true}.
	 * <li>It is <i>symmetric</i>: for any non-null reference values
	 *     {@code x} and {@code y}, {@code x.equals(y)}
	 *     should return {@code true} if and only if
	 *     {@code y.equals(x)} returns {@code true}.
	 * <li>It is <i>transitive</i>: for any non-null reference values
	 *     {@code x}, {@code y}, and {@code z}, if
	 *     {@code x.equals(y)} returns {@code true} and
	 *     {@code y.equals(z)} returns {@code true}, then
	 *     {@code x.equals(z)} should return {@code true}.
	 * <li>It is <i>consistent</i>: for any non-null reference values
	 *     {@code x} and {@code y}, multiple invocations of
	 *     {@code x.equals(y)} consistently return {@code true}
	 *     or consistently return {@code false}, provided no
	 *     information used in {@code equals} comparisons on the
	 *     objects is modified.
	 * <li>For any non-null reference value {@code x},
	 *     {@code x.equals(null)} should return {@code false}.
	 * </ul>
	 *
	 * <p>
	 * An equivalence relation partitions the elements it operates on
	 * into <i>equivalence classes</i>; all the members of an
	 * equivalence class are equal to each other. Members of an
	 * equivalence class are substitutable for each other, at least
	 * for some purposes.
	 *
	 * @param obj the reference object with which to compare.
	 * @return {@code true} if this object is the same as the obj
	 * argument; {@code false} otherwise.
	 * @implSpec The {@code equals} method for class {@code Object} implements
	 * the most discriminating possible equivalence relation on objects;
	 * that is, for any non-null reference values {@code x} and
	 * {@code y}, this method returns {@code true} if and only
	 * if {@code x} and {@code y} refer to the same object
	 * ({@code x == y} has the value {@code true}).
	 * <p>
	 * In other words, under the reference equality equivalence
	 * relation, each equivalence class only has a single element.
	 * @apiNote It is generally necessary to override the {@link #hashCode() hashCode}
	 * method whenever this method is overridden, so as to maintain the
	 * general contract for the {@code hashCode} method, which states
	 * that equal objects must have equal hash codes.
	 * <p>The two-argument {@link Objects#equals(Object,
	 * Object) Objects.equals} method implements an equivalence relation
	 * on two possibly-null object references.
	 * @see #hashCode()
	 * @see HashMap
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof LocaleDir file)) return false;

		try {
			return Files.isSameFile(this.path, file.path);
		} catch (IOException ex) {
			return this.path.toAbsolutePath().toString()
					.equals(file.path.toAbsolutePath().toString());
		}
	}

	/**
	 * {@return a string representation of the object}
	 * <p>
	 * Satisfying this method's contract implies a non-{@code null}
	 * result must be returned.
	 *
	 * @apiNote In general, the
	 * {@code toString} method returns a string that
	 * "textually represents" this object. The result should
	 * be a concise but informative representation that is easy for a
	 * person to read.
	 * It is recommended that all subclasses override this method.
	 * The string output is not necessarily stable over time or across
	 * JVM invocations.
	 * @implSpec The {@code toString} method for class {@code Object}
	 * returns a string consisting of the name of the class of which the
	 * object is an instance, the at-sign character `{@code @}', and
	 * the unsigned hexadecimal representation of the hash code of the
	 * object. In other words, this method returns a string equal to the
	 * value of:
	 * {@snippet lang = java:
	 * getClass().getName() + '@' + Integer.toHexString(hashCode())
	 *}
	 * The {@link Objects#toIdentityString(Object)
	 * Objects.toIdentityString} method returns the string for an
	 * object equal to the string that would be returned if neither
	 * the {@code toString} nor {@code hashCode} methods were
	 * overridden by the object's class.
	 */
	@Override
	public String toString() {
		return this.path.toAbsolutePath().toString();
	}

	/**
	 * {@return a hash code value for this object} This method is
	 * supported for the benefit of hash tables such as those provided by
	 * {@link HashMap}.
	 * <p>
	 * The general contract of {@code hashCode} is:
	 * <ul>
	 * <li>Whenever it is invoked on the same object more than once during
	 *     an execution of a Java application, the {@code hashCode} method
	 *     must consistently return the same integer, provided no information
	 *     used in {@code equals} comparisons on the object is modified.
	 *     This integer need not remain consistent from one execution of an
	 *     application to another execution of the same application.
	 * <li>If two objects are equal according to the {@link
	 *     #equals(Object) equals} method, then calling the {@code
	 *     hashCode} method on each of the two objects must produce the
	 *     same integer result.
	 * <li>It is <em>not</em> required that if two objects are unequal
	 *     according to the {@link #equals(Object) equals} method, then
	 *     calling the {@code hashCode} method on each of the two objects
	 *     must produce distinct integer results.  However, the programmer
	 *     should be aware that producing distinct integer results for
	 *     unequal objects may improve the performance of hash tables.
	 * </ul>
	 *
	 * @implSpec As far as is reasonably practical, the {@code hashCode} method defined
	 * by class {@code Object} returns distinct integers for distinct objects.
	 * @apiNote The {@link Objects#hash(Object...) hash} and {@link
	 * Objects#hashCode(Object) hashCode} methods of {@link
	 * Objects} can be used to help construct simple hash codes.
	 * @see Object#equals(Object)
	 * @see System#identityHashCode
	 */
	@Override
	public int hashCode() {
		try {
			Path real = this.path.toRealPath();
			return Objects.hashCode(real);
		} catch (IOException ex) {
			return Objects.hashCode(this.path.toAbsolutePath().normalize());
		}
	}

	private static Path resolvePath(String... route) {
		if (route != null && route.length > 0) {
			String[] clone = new String[route.length + 1];
			System.arraycopy(route, 0, clone, 1, route.length);
			clone[0] = "i18n";

			return Path.of("LockLogin", clone);
		}

		return Path.of("LockLogin", "i18n");
	}

	/**
	 * Create an absolute localization file
	 * @param path the file path
	 * @return the localization file
	 */
	@Contract("null -> fail")
	public static LocaleDir absolute(final Path path) {
		Path normalized = Objects.requireNonNull(path).normalize();
		if (!normalized.isAbsolute()) {
			throw new IllegalArgumentException("LocaleFile#absolute requires an absolute path too");
		}

		return new LocaleDir(normalized);
	}
}
