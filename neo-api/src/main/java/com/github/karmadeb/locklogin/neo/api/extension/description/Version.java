package com.github.karmadeb.locklogin.neo.api.extension.description;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * Represents a SemVer version
 */
public class Version implements Comparable<Version> {

	public static final Comparator<Version> COMPARATOR = Version::compareTo;

	private final int major;
	private final int minor;
	private final Integer patch;
	private final String data;
	private final Type type;

	/**
	 * Create a new version
	 * @param major the major version
	 * @param minor the minor version
	 * @param patch the patch version
	 * @param data the version data
	 */
	public Version(final int major, final int minor, final Integer patch, final String data) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.data = data;
		this.type = Type.match(data).orElse(Type.RELEASE);
	}

	/**
	 * Create a new version
	 * @param major the major version
	 * @param minor the minor version
	 * @param data the version data
	 */
	public Version(final int major, final int minor, final String data) {
		this(major, minor, null, data);
	}

	/**
	 * Create a new version
	 * @param major the major version
	 * @param minor the minor version
	 */
	public Version(final int major, final int minor) {
		this(major, minor, null, null);
	}

	/**
	 * Create a new version
	 * @param major the major version
	 * @param minor the minor version
	 * @param patch the patch version
	 */
	public Version(final int major, final int minor, final int patch) {
		this(major, minor, patch, null);
	}

	/**
	 * Get the version major
	 * @return the major
	 */
	public int major() {
		return this.major;
	}

	/**
	 * Get the version minor
	 * @return the minor
	 */
	public int minor() {
		return this.minor;
	}

	/**
	 * Get the version patch
	 * @return the version patch
	 */
	public int patch() {
		return this.patch == null ? 0 : this.patch;
	}

	/**
	 * Get the version data
	 * @return the version data
	 */
	public String data() {
		return this.data == null ? "" : this.data;
	}

	/**
	 * Get the version type
	 * @return the version type
	 */
	public Type type() {
		return this.type == null ? Type.RELEASE : this.type;
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
		if (!(obj instanceof Version ver)) return false;

		return Objects.equals(this.major, ver.major) &&
				Objects.equals(this.minor, ver.minor) &&
				Objects.equals(this.patch(), ver.patch()) &&
				this.data().equalsIgnoreCase(ver.data());
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
		return Objects.hash(this.major, this.minor, this.patch(), this.data().toLowerCase());
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
		StringBuilder builder = new StringBuilder();
		builder.append(this.major).append('.').append(this.minor);

		if (this.patch != null) {
			builder.append('.').append(this.patch);
		}
		if (this.data != null) {
			builder.append('-').append(this.data);
		}

		return builder.toString();
	}

	/**
	 * The version type helps developers and LockLogin itself
	 * to detect potentially unstable extensions. "Stable" are considered
	 * CANDIDATE and RELEASE, being SNAPSHOT the only "unstable".
	 * When the plugin cannot identify the version type, it is provided
	 * the plugin assumes {@link #RELEASE} as most developers simply stop adding
	 * or including version type data once release is reached (see Minecraft or
	 * LockLogin itself versions as an example).
	 */
	public enum Type {
		RELEASE(2, "release", "final", "stable"),
		CANDIDATE(1, "rc", "beta", "release-candidate",
				"candidate", "pre", "prerelease", "pre-release",
				"prestable", "pre-stable"),
		SNAPSHOT(0, "alpha", "unstable", "snapshot", "untested");

		private final int weight;
		private final String[] keywords;

		/**
		 * Create a new version type
		 * @param weight the type weight
		 * @param keywords the type detection keywords
		 */
		Type(final int weight, final String... keywords) {
			this.weight = weight;
			this.keywords = keywords;
		}

		/**
		 * Get the version type weight
		 * @return the weight
		 */
		public int weight() {
			return this.weight;
		}

		/**
		 * Get the version type keywords
		 * @return the keywords
		 */
		public @NotNull @Unmodifiable List<String> keywords() {
			return Arrays.asList(this.keywords);
		}

		/**
		 * Tries to match version data to determine the
		 * version type
		 * @param data the version data
		 * @return the version type
		 */
		public static Optional<Type> match(final String data) {
			if (data == null || data.isBlank()) {
				return Optional.empty();
			}

			String[] verData = data.toLowerCase().split("[_.,\\s]");
			for (String word : verData) {
				for (Type type : Type.values()) {
					if (type.keywords().stream()
							.map(String::toLowerCase)
							.anyMatch(word::contains)) {
						return Optional.of(type);
					}
				}
			}

			return Optional.empty();
		}
	}

	/**
	 * Compares this object with the specified object for order.  Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 *
	 * <p>The implementor must ensure {@link Integer#signum
	 * signum}{@code (x.compareTo(y)) == -signum(y.compareTo(x))} for
	 * all {@code x} and {@code y}.  (This implies that {@code
	 * x.compareTo(y)} must throw an exception if and only if {@code
	 * y.compareTo(x)} throws an exception.)
	 *
	 * <p>The implementor must also ensure that the relation is transitive:
	 * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
	 * {@code x.compareTo(z) > 0}.
	 *
	 * <p>Finally, the implementor must ensure that {@code
	 * x.compareTo(y)==0} implies that {@code signum(x.compareTo(z))
	 * == signum(y.compareTo(z))}, for all {@code z}.
	 *
	 * @param other the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object
	 * is less than, equal to, or greater than the specified object.
	 * @throws NullPointerException if the specified object is null
	 * @throws ClassCastException   if the specified object's type prevents it
	 *                              from being compared to this object.
	 * @apiNote It is strongly recommended, but <i>not</i> strictly required that
	 * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
	 * class that implements the {@code Comparable} interface and violates
	 * this condition should clearly indicate this fact.  The recommended
	 * language is "Note: this class has a natural ordering that is
	 * inconsistent with equals."
	 */
	@Override
	public int compareTo(final @NotNull Version other) {
		int thisWeight = this.type.weight();
		int otherWeight = other.type.weight();

		int majorComp = Integer.compare(this.major, other.major);
		int minorComp = Integer.compare(this.minor, other.minor);
		int patchComp = Integer.compare(this.patch(), other.patch());

		if (majorComp != 0) return majorComp;
		if (minorComp != 0) return minorComp;
		if (patchComp != 0) return patchComp;

		return Integer.compare(thisWeight, otherWeight);
	}
}
