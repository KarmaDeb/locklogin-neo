package com.github.karmadeb.locklogin.neo.api.security.hash;

import com.github.karmadeb.locklogin.neo.api.security.HashMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.security.MessageDigest;
import java.util.*;

/**
 * A hash represents the computed result of a {@link HashMethod} applied
 * to an input string
 */
public abstract class Hash {

	protected final byte[] hash;
	protected final byte[] salt;
	protected final HashProperties properties;

	/**
	 * Create a new hash
	 * @param hash the hash
	 * @param salt the salt
	 * @param properties the properties
	 */
	protected Hash(final byte @NotNull[] hash,
				   final byte @NotNull[] salt,
				   final @NotNull HashProperties properties) {
		this.hash = Objects.requireNonNull(hash);
		this.salt = Objects.requireNonNull(salt);
		this.properties = Objects.requireNonNull(properties);
	}

	/**
	 * When an algorithm is deprecated, it means is considered to
	 * be not secure and its existence is provided only for
	 * retro compatibility reasons. When an algorithm is marked
	 * as deprecated and is actively enabled (used as any form of
	 * default hashing algorithm) two things will happen:
	 * <ul>
	 *     <li>
	 *         The default non deprecated hashing algorithm will be enabled (Argon2)
	 *     </li>
	 *     <li>
	 *         A warning is printed to console notifying about the previous action and that the
	 *         algorithm is not secure
	 *     </li>
	 * </ul>
	 * Instantiating or using the algorithm itself will not print any form of warning, except for
	 * the first time used/instantiated. It is also recommended to annotate the class itself with
	 * the {@link Deprecated deprecated} annotation in order for better IDE support.
	 * @return if the algorithm is deprecated
	 */
	public boolean deprecated() {
		return false;
	}

	/**
	 * Get the hash
	 * @return the hash
	 */
	public final byte @NotNull[] hash() {
		return this.hash.clone();
	}

	/**
	 * Get the salt
	 * @return the salt
	 */
	public final byte @NotNull[] salt() {
		return this.salt.clone();
	}

	/**
	 * Get the hash properties
	 * @return the properties
	 */
	public final @NotNull @UnmodifiableView HashProperties properties() {
		return new HashProperties(this.properties);
	}

	/**
	 * Check if the hash matches the other
	 * hash
	 * @param obj   the reference object with which to compare.
	 * @return if the hashes match
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Hash other)) return false;

		return MessageDigest.isEqual(this.hash, other.hash) &&
				MessageDigest.isEqual(this.salt, other.salt);
	}

	/**
	 * Hash the hash instance
	 * @return the hashed instance
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.hash) +
				Arrays.hashCode(this.salt) +
				Objects.hashCode(this.properties);
	}

	/**
	 * Get the string representation of the hash using
	 * hex for byte serialization
	 * @return the string representation of the
	 * computed hash
	 */
	@Override
	public String toString() {
		HexFormat format = HexFormat.of();

		StringBuilder builder = new StringBuilder("$");
		builder.append(this.properties).append("$");

		if (this.salt.length > 0) {
			builder.append(format.formatHex(this.salt))
					.append("$");
		}

		builder.append(format.formatHex(this.hash))
				.append("$");

		return builder.toString();
	}

	/**
	 * Create a computed hash
	 * @param hash the hash
	 * @param salt the salt
	 * @param properties the properties
	 * @return the hash
	 */
	static Hash computed(final byte @NotNull[] hash,
						 final byte @NotNull[] salt,
						 final @NotNull HashProperties properties) {
		return new Hash(hash, salt, properties) {};
	}

	/**
	 * Parse a hash from a raw hash string
	 * @param raw the hash
	 * @return the parsed hash
	 */
	public static Hash fromString(final String raw) {
		String hash = raw.startsWith("$") ? raw.substring(1) : raw;
		hash = hash.endsWith("$") ? hash.substring(0, hash.length() - 1) : hash;

		String[] parts = hash.split("\\$");
		HashProperties properties = HashProperties.fromString(parts[0]);

		HexFormat format = HexFormat.of();

		String computedHash = parts[parts.length - 1];
		byte[] h = format.parseHex(computedHash);

		if (parts.length == 3) {
			String salt = parts[1];
			if (!salt.contains("=")) {
				byte[] s = format.parseHex(salt);
				return computed(h, s, properties);
			}
		}

		return computed(h, new byte[0], properties);
	}
}
