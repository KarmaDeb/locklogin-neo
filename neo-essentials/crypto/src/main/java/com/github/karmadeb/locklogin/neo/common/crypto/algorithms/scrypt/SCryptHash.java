package com.github.karmadeb.locklogin.neo.common.crypto.algorithms.scrypt;

import com.github.karmadeb.locklogin.neo.api.security.hash.Hash;
import com.github.karmadeb.locklogin.neo.api.security.hash.HashProperties;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.scrypt.exception.NotSCryptHashException;
import com.github.karmadeb.locklogin.neo.common.crypto.config.scrypt.SCryptConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


public final class SCryptHash extends Hash {

	/**
	 * Create a new hash
	 *
	 * @param hash       the hash
	 * @param salt       the salt
	 * @param properties the properties
	 */
	@Contract("null, _, _ -> fail; _, null, _ -> fail; _, _, null -> fail")
	public SCryptHash(final byte [] hash,
					  final byte [] salt,
					  final HashProperties properties) {
		super(hash, salt, properties);
	}

	public int cost() {
		return this.properties.getInt("c", SCryptConfiguration.DEFAULT_COST);
	}

	public int block() {
		return this.properties.getInt("b", SCryptConfiguration.DEFAULT_BLOCK_SIZE);
	}

	public int parallelism() {
		return this.properties.getInt("p", SCryptConfiguration.DEFAULT_PARALLELISM);
	}

	public boolean outdated(final @NotNull HashProperties o) {
		return this.outdated(o, false);
	}

	public boolean outdated(final @NotNull HashProperties o, final boolean strict) throws NotSCryptHashException {
		int otherCost = o.getInt("c");
		if (otherCost <= 0) {
			throw new NotSCryptHashException("Cannot compare scrypt hash with non-scrypt hash properties (missing \"c\")");
		}

		int otherBlock = o.getInt("b");
		if (otherBlock <= 0) {
			throw new NotSCryptHashException("Cannot compare scrypt hash with non-scrypt hash properties (missing \"b\")");
		}

		int otherParallelism = o.getInt("p");
		if (otherParallelism <= 0) {
			throw new NotSCryptHashException("Cannot compare scrypt hash with non-scrypt hash properties (missing \"p\")");
		}

		int cost = this.cost();
		int block = this.block();
		int parallelism = this.parallelism();

		if (strict) {
			return otherCost != cost || otherBlock != block || otherParallelism != parallelism;
		}

		return otherCost > cost || otherBlock > block || otherParallelism > parallelism;
	}

	/**
	 * Adopts any kind of hash as a scrypt hash
	 * @param hash the hash
	 * @return the scrypt hash
	 */
	public static SCryptHash adopt(final Hash hash) throws NotSCryptHashException {
		if (hash instanceof SCryptHash bcrypt) return bcrypt;

		HashProperties properties = hash.properties();
		int cost = properties.getInt("c", SCryptConfiguration.DEFAULT_COST);
		if (!SCryptConfiguration.isValidCost(cost)) {
			throw new NotSCryptHashException("Bad scrypt hash with non-scrypt hash properties (missing \"c\")");
		}

		int block = properties.getInt("b", SCryptConfiguration.DEFAULT_BLOCK_SIZE);
		if (!SCryptConfiguration.isValidPositiveInt(block)) {
			throw new NotSCryptHashException("Bad scrypt hash with non-scrypt hash properties (missing \"b\")");
		}

		int parallelism = properties.getInt("p", SCryptConfiguration.DEFAULT_PARALLELISM);
		if (!SCryptConfiguration.isValidPositiveInt(parallelism)) {
			throw new NotSCryptHashException("Bad scrypt hash with non-scrypt hash properties (missing \"p\")");
		}

		return new SCryptHash(hash.hash(), hash.salt(), HashProperties.cloneOf(properties));
	}
}
