package com.github.karmadeb.locklogin.neo.common.crypto.algorithms.scrypt;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.security.HashMethod;
import com.github.karmadeb.locklogin.neo.api.security.hash.Hash;
import com.github.karmadeb.locklogin.neo.api.security.hash.HashProperties;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.scrypt.exception.NotSCryptHashException;
import com.github.karmadeb.locklogin.neo.common.crypto.config.scrypt.SCryptConfiguration;
import org.bouncycastle.crypto.generators.SCrypt;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SCryptHashMethod implements HashMethod {

	public static final int MAGIC_NUMBERS_DK_LEN = 32;
	private final SCryptConfiguration configuration;

	public SCryptHashMethod(final SCryptConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Get the hashing method id. The ID can be
	 * repeated across extensions, but not in the
	 * same extension
	 *
	 * @return the hash method id
	 */
	@Override
	public @NotNull NamespacedIdentity id() {
		return NamespacedIdentity.locklogin("scrypt");
	}

	/**
	 * Compute the hash method properties
	 *
	 * @return the properties
	 */
	@Override
	public @NotNull HashProperties computeProperties() {
		HashProperties properties = new HashProperties();
		properties.set("c", this.configuration.cost());
		properties.set("b", this.configuration.block());
		properties.set("p", this.configuration.parallelism());

		return properties;
	}

	@Override
	public boolean verify(final @NotNull Hash hash,
						  final @NotNull String input) {
		SCryptHash result = this.hash(input, hash.salt(), hash.properties());
		return MessageDigest.isEqual(result.hash(), hash.hash());
	}

	@Override
	public boolean requiresUpdate(final @NotNull Hash hash) {
		if (!(hash instanceof SCryptHash scrypt)) return true;

		HashProperties properties = this.computeProperties();
		return scrypt.outdated(properties);
	}

	@Override
	public @NotNull SCryptHash hash(final @NotNull String input,
									final byte @NotNull [] salt,
									final @NotNull HashProperties properties) throws NotSCryptHashException {
		int cost = properties.getInt("c", SCryptConfiguration.DEFAULT_COST);
		if (!SCryptConfiguration.isValidCost(cost)) {
			throw new NotSCryptHashException("Cannot create scrypt hash with non-scrypt hash properties (missing \"c\")");
		}

		int block = properties.getInt("b", SCryptConfiguration.DEFAULT_BLOCK_SIZE);
		if (!SCryptConfiguration.isValidPositiveInt(block)) {
			throw new NotSCryptHashException("Cannot create scrypt hash with non-scrypt hash properties (missing \"b\")");
		}

		int parallelism = properties.getInt("p", SCryptConfiguration.DEFAULT_PARALLELISM);
		if (!SCryptConfiguration.isValidPositiveInt(parallelism)) {
			throw new NotSCryptHashException("Cannot create scrypt hash with non-scrypt hash properties (missing \"p\")");
		}

		byte[] hash = SCrypt.generate(input.getBytes(StandardCharsets.UTF_8), salt, cost, block,
				parallelism, MAGIC_NUMBERS_DK_LEN);
		return new SCryptHash(hash, salt, properties);
	}

	@Override
	public SCryptHash parse(final @NotNull String hash) {
		Hash parsed = Hash.fromString(hash);
		return SCryptHash.adopt(parsed);
	}
}
