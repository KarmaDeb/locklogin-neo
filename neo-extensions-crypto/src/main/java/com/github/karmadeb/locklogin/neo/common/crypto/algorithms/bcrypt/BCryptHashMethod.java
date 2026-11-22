package com.github.karmadeb.locklogin.neo.common.crypto.algorithms.bcrypt;

import com.github.karmadeb.locklogin.neo.api.Constants;
import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.security.HashMethod;
import com.github.karmadeb.locklogin.neo.api.security.hash.Hash;
import com.github.karmadeb.locklogin.neo.api.security.hash.HashProperties;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.bcrypt.exception.NotBCryptHashException;
import com.github.karmadeb.locklogin.neo.common.crypto.config.bcrypt.BCryptConfiguration;
import org.bouncycastle.crypto.generators.BCrypt;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;

public class BCryptHashMethod implements HashMethod {

	private final BCryptConfiguration configuration;

	public BCryptHashMethod(final BCryptConfiguration configuration) {
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
		return NamespacedIdentity.create(Constants.LOCKLOGIN_EXTENSION_ID, "bcrypt");
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

		return properties;
	}

	@Override
	public boolean verify(final @NotNull Hash hash,
						  final @NotNull String input) {
		BcryptHash result = this.hash(input, hash.salt(), hash.properties());
		return MessageDigest.isEqual(result.hash(), hash.hash());
	}

	@Override
	public boolean requiresUpdate(final @NotNull Hash hash) {
		if (!(hash instanceof BcryptHash bcrypt)) return true;

		HashProperties properties = this.computeProperties();
		return bcrypt.outdated(properties);
	}

	@Override
	public @NotNull BcryptHash hash(final @NotNull String input,
									final byte @NotNull [] salt,
									final @NotNull HashProperties properties) throws NotBCryptHashException {
		int cost = properties.getInt("c", BCryptConfiguration.DEFAULT_COST);
		if (!BCryptConfiguration.isValidBcryptCost(cost)) {
			throw new NotBCryptHashException("Cannot create bcrypt hash with non-bcrypt hash properties (missing \"c\")");
		}

		byte[] password = BCrypt.passwordToByteArray(input.toCharArray());
		byte[] hash = BCrypt.generate(password, salt, cost, false);

		return new BcryptHash(hash, salt, properties);
	}

	@Override
	public BcryptHash parse(final @NotNull String hash) {
		Hash parsed = Hash.fromString(hash);
		return BcryptHash.adopt(parsed);
	}
}
