package com.github.karmadeb.locklogin.neo.common.crypto.algorithms.bcrypt;

import com.github.karmadeb.locklogin.neo.api.security.hash.Hash;
import com.github.karmadeb.locklogin.neo.api.security.hash.HashProperties;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.bcrypt.exception.NotBCryptHashException;
import com.github.karmadeb.locklogin.neo.common.crypto.config.bcrypt.BCryptConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class BcryptHash extends Hash {

	/**
	 * Create a new hash
	 *
	 * @param hash       the hash
	 * @param salt       the salt
	 * @param properties the properties
	 */
	@Contract("null, _, _ -> fail; _, null, _ -> fail; _, _, null -> fail")
	public BcryptHash(final byte [] hash,
					  final byte [] salt,
					  final HashProperties properties) {
		super(hash, salt, properties);
	}

	public int cost() {
		return this.properties.getInt("c", BCryptConfiguration.DEFAULT_COST);
	}

	public boolean outdated(final @NotNull HashProperties o) {
		return this.outdated(o, false);
	}

	public boolean outdated(final @NotNull HashProperties o, final boolean strict) throws NotBCryptHashException {
		int otherCost = o.getInt("c");
		if (otherCost <= 0) {
			throw new NotBCryptHashException("Cannot compare bcrypt hash with non-bcrypt hash properties (missing \"c\")");
		}

		int cost = this.cost();
		return strict ? otherCost != cost : otherCost > cost;
	}

	/**
	 * Adopts any kind of hash as a bcrypt hash
	 * @param hash the hash
	 * @return the bcrypt hash
	 */
	public static BcryptHash adopt(final Hash hash) throws NotBCryptHashException {
		if (hash instanceof BcryptHash bcrypt) return bcrypt;

		HashProperties properties = hash.properties();
		int cost = properties.getInt("c", BCryptConfiguration.DEFAULT_COST);
		if (!BCryptConfiguration.isValidBcryptCost(cost)) {
			throw new NotBCryptHashException("Bad bcrypt hash with non-bcrypt hash properties (missing \"c\")");
		}

		return new BcryptHash(hash.hash(), hash.salt(), HashProperties.cloneOf(properties));
	}
}
