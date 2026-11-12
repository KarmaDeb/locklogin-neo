package com.github.karmadeb.locklogin.neo.common.security.methods.argon;

import com.github.karmadeb.locklogin.neo.api.security.hash.Hash;
import com.github.karmadeb.locklogin.neo.api.security.hash.HashProperties;
import com.github.karmadeb.locklogin.neo.common.security.methods.argon.exception.NotArgonHashException;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


public final class Argon2Hash extends Hash {

	public static final int DEFAULT_ITERATIONS = 2;
	public static final int DEFAULT_MEMORY = 16;
	public static final int DEFAULT_PARALLELISM = 1;

	/**
	 * Create a new hash
	 *
	 * @param hash       the hash
	 * @param salt       the salt
	 * @param properties the properties
	 */
	@Contract("null, _, _ -> fail; _, null, _ -> fail; _, _, null -> fail")
	public Argon2Hash(final byte [] hash,
					  final byte [] salt,
					  final HashProperties properties) {
		super(hash, salt, properties);
	}

	public Parameter argon() {
		int argon = this.properties.getInt("a", Parameter.ARGON2_ID.argon());
		return Parameter.fromArgon(argon);
	}

	public Version version() {
		int version = this.properties.getInt("v", Version.VERSION_13.version());
		return Version.get(version);
	}

	public int iterations() {
		return this.properties.getInt("i", DEFAULT_ITERATIONS);
	}

	public int memory() {
		return this.properties.getInt("m", DEFAULT_MEMORY);
	}

	public int parallelism() {
		return this.properties.getInt("p", DEFAULT_PARALLELISM);
	}

	public boolean outdated(final @NotNull HashProperties o) {
		return this.outdated(o, false);
	}

	public boolean outdated(final @NotNull HashProperties o, final boolean strict) throws NotArgonHashException {
		int otherArgon = o.getInt("a");
		if (otherArgon <= 0) {
			throw new NotArgonHashException("Cannot compare argon2 hash with non-argon hash properties (missing \"a\")");
		}

		int otherVersion = o.getInt("v");
		if (otherVersion <= 0) {
			throw new NotArgonHashException("Cannot compare argon2 hash with non-argon hash properties (missing \"v\")");
		}

		int otherIterations = o.getInt("i");
		if (otherIterations <= 0) {
			throw new NotArgonHashException("Cannot compare argon2 hash with non-argon hash properties (missing \"i\")");
		}

		int otherMemory = o.getInt("m");
		if (otherMemory <= 0) {
			throw new NotArgonHashException("Cannot compare argon2 hash with non-argon hash properties (missing \"m\")");
		}

		int otherParallelism = o.getInt("p");
		if (otherParallelism <= 0) {
			throw new NotArgonHashException("Cannot compare argon2 hash with non-argon hash properties (missing \"p\")");
		}

		int argon = this.argon().argon();
		int version = this.version().version();
		int iterations = this.iterations();
		int memory = this.memory();
		int parallelism = this.parallelism();

		if (argon != otherArgon || version != otherVersion) {
			return true;
		}

		if (!strict) {
			return iterations < otherIterations || memory < otherMemory || parallelism < otherParallelism;
		}

		return iterations != otherIterations || memory != otherMemory || parallelism != otherParallelism;
	}

	public enum Parameter {
		ARGON2_D(Argon2Parameters.ARGON2_d),
		ARGON2_I(Argon2Parameters.ARGON2_i),
		ARGON2_ID(Argon2Parameters.ARGON2_id);

		private final int argon;

		Parameter(final int argon) {
			this.argon = argon;
		}

		public int argon() {
			return this.argon;
		}

		public static Parameter fromArgon(final int argon) {
			if (argon == Argon2Parameters.ARGON2_d) {
				return Parameter.ARGON2_D;
			} else if (argon == Argon2Parameters.ARGON2_i) {
				return Parameter.ARGON2_I;
			}

			return Parameter.ARGON2_ID;
		}
	}

	/**
	 * Argon2 version
	 */
	public enum Version {
		VERSION_10(Argon2Parameters.ARGON2_VERSION_10),
		VERSION_13(Argon2Parameters.ARGON2_VERSION_13);

		private final int version;

		Version(final int version) {
			this.version = version;
		}

		public int version() {
			return this.version;
		}

		public static Version get(final int version) {
			if (version == Argon2Parameters.ARGON2_VERSION_10) {
				return Version.VERSION_10;
			}

			return Version.VERSION_13;
		}
	}

	/**
	 * Adopts any kind of hash as an argon2 hash
	 * @param hash the hash
	 * @return the argon2 hash
	 */
	public static Argon2Hash adopt(final Hash hash) throws NotArgonHashException {
		if (hash instanceof Argon2Hash argon) return argon;

		HashProperties properties = hash.properties();
		int argonId = properties.getInt("a", Parameter.ARGON2_ID.argon());
		if (argonId <= 0) {
			throw new NotArgonHashException("Bad argon2 hash with non-argon hash properties (missing \"a\")");
		}

		int argonVersion = properties.getInt("v", Version.VERSION_13.version());
		if (argonVersion <= 0) {
			throw new NotArgonHashException("Bad argon2 hash with non-argon hash properties (missing \"v\")");
		}

		int iterations = properties.getInt("i", Argon2Hash.DEFAULT_ITERATIONS);
		if (iterations <= 0) {
			throw new NotArgonHashException("Bad argon2 hash with non-argon hash properties (missing \"i\")");
		}

		int memory = properties.getInt("m", Argon2Hash.DEFAULT_MEMORY);
		if (memory <= 0) {
			throw new NotArgonHashException("Bad argon2 hash with non-argon hash properties (missing \"m\")");
		}

		int parallelism = properties.getInt("p", Argon2Hash.DEFAULT_PARALLELISM);
		if (parallelism <= 0) {
			throw new NotArgonHashException("Bad argon2 hash with non-argon hash properties (missing \"p\")");
		}

		return new Argon2Hash(hash.hash(), hash.salt(), HashProperties.cloneOf(properties));
	}
}
