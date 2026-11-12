package com.github.karmadeb.locklogin.neo.api.configuration.registry;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents a configuration file. Does not allow
 * by himself to read, nor save the file, but it helps
 * to it
 */
public final class ConfigFile {

	public static final ConfigFile PLUGIN_CONFIG = new ConfigFile("config");

	private final Path path;

	/**
	 * Create a new config file
	 * @param filename the config file name
	 * @param route the config file route
	 */
	@Contract("null, _ -> fail")
	public ConfigFile(final String filename, final String... route) {
		this.path = ConfigFile.resolvePath(filename, route);
	}

	/**
	 * Get the file path
	 * @return the path
	 */
	@NotNull
	public Path path() {
		return this.path;
	}

	/**
	 * Open a stream to read the file
	 * @return the stream
	 * @throws IOException if the path fails to open
	 */
	@NotNull
	public InputStream read() throws IOException {
		Path parent = this.path.getParent();
		if (!Files.exists(parent)) {
			Files.createDirectories(parent);
		}

		return Files.newInputStream(this.path, StandardOpenOption.CREATE);
	}

	/**
	 * Open a stream to write to the file
	 * @return the stream
	 * @throws IOException if the path fails to open
	 */
	public OutputStream write() throws IOException {
		Path parent = this.path.getParent();
		if (!Files.exists(parent)) {
			Files.createDirectories(parent);
		}

		return Files.newOutputStream(this.path, StandardOpenOption.CREATE);
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
		if (!(obj instanceof ConfigFile file)) return false;

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

	private static Path resolvePath(final String filename, final String... route) {
		if (route != null && route.length > 0) {
			return Path.of("LockLogin", route).resolve(String.format("%s.yaml", filename));
		}

		return Path.of("LockLogin", String.format("%s.yaml", filename));
	}
}
