package com.github.karmadeb.locklogin.neo.api.security;

import com.github.karmadeb.locklogin.neo.api.Constants;
import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.security.hash.Hash;
import com.github.karmadeb.locklogin.neo.api.security.hash.HashProperties;
import org.jetbrains.annotations.NotNull;

/**
 * A hash method defines the basic structure a password hashing
 * algorithm should have in order to be used in LockLogin-Neo.
 * This allows plugin developers to support more hashing methods
 * than the plugin defaults without requiring it to update
 */
public interface HashMethod {

	/**
	 * Get the hashing method id. The ID can be
	 * repeated across extensions, but not in the
	 * same extension
	 * @return the hash method id
	 */
	@NotNull NamespacedIdentity id();

	/**
	 * Compute the hash method properties
	 * @return the properties
	 */
	default @NotNull HashProperties computeProperties() {
		return new HashProperties();
	}

	/**
	 * Verify the hash with the input
	 * @param hash the hash
	 * @param input the input
	 * @return if the hashes match
	 */
	default boolean verify(final @NotNull Hash hash, final @NotNull String input) {
		Hash reHash = this.hash(input, hash.salt(), hash.properties());
		return hash.equals(reHash);
	}

	/**
	 * Check if a hash requires an update based on
	 * its properties. As most hashes does not require
	 * an update after hashing, this method by default
	 * always returns false. While other algorithms such
	 * as Argon2 algorithm might need to overwrite this behavior
	 * @param hash the hash
	 * @return if the hash is outdated
	 */
	default boolean requiresUpdate(final @NotNull Hash hash) {
		return false;
	}

	/**
	 * Create a new hash for the specified input
	 * @param input the input
	 * @return the hash
	 */
	default @NotNull Hash hash(final @NotNull String input) {
		byte[] salt = new byte[16];
		Constants.RANDOM.nextBytes(salt);

		return this.hash(input, salt, this.computeProperties());
	}

	/**
	 * Create a new hash for the specified input
	 * @param input the input
	 * @param salt the salt
	 * @return the hash
	 */
	default @NotNull Hash hash(final @NotNull String input,
							   final byte @NotNull[] salt) {
		return this.hash(input, salt, this.computeProperties());
	}

	/**
	 * Create a new hash for the specified input
	 * @param input the input
	 * @param properties the hash properties
	 * @return the hash
	 */
	default @NotNull Hash hash(final @NotNull String input,
							   final @NotNull HashProperties properties) {
		byte[] salt = new byte[16];
		Constants.RANDOM.nextBytes(salt);

		return this.hash(input, salt, properties);
	}

	/**
	 * Create a new hash for the specified input
	 * @param input the input
	 * @param salt the salt
	 * @param properties the hash properties
	 * @return the hash
	 */
	@NotNull Hash hash(final @NotNull String input,
					   final byte @NotNull[] salt,
					   final @NotNull HashProperties properties);

	/**
	 * Parse a hash string into a hash instance
	 * @param hash the hash
	 * @return the parsed hash
	 */
	default Hash parse(final @NotNull String hash) {
		return Hash.fromString(hash);
	}
}
