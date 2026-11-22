package com.github.karmadeb.locklogin.neo.common.crypto.algorithms.argon;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.security.HashMethod;
import com.github.karmadeb.locklogin.neo.api.security.hash.Hash;
import com.github.karmadeb.locklogin.neo.api.security.hash.HashProperties;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.argon.Argon2Hash.Parameter;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.argon.Argon2Hash.Version;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.argon.exception.NotArgonHashException;
import com.github.karmadeb.locklogin.neo.common.crypto.config.argon.Argon2Configuration;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;


public class Argon2HashMethod implements HashMethod {

	private final Argon2Configuration configuration;

	public Argon2HashMethod(final Argon2Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public @NotNull NamespacedIdentity id() {
		return NamespacedIdentity.locklogin("argon2");
	}

	@Override
	public @NotNull HashProperties computeProperties() {
		HashProperties properties = new HashProperties();
		properties.set("a", this.configuration.parameter().argon());
		properties.set("v", this.configuration.version().version());
		properties.set("i", this.configuration.iterations());
		properties.set("m", this.configuration.memory());
		properties.set("p", this.configuration.parallelism());

		return properties;
	}

	@Override
	public boolean verify(final @NotNull Hash hash,
						  final @NotNull String input) {
		Argon2Hash result = this.hash(input, hash.salt(), hash.properties());
		return MessageDigest.isEqual(result.hash(), hash.hash());
	}

	@Override
	public boolean requiresUpdate(final @NotNull Hash hash) {
		if (!(hash instanceof Argon2Hash argon)) return true;

		HashProperties properties = this.computeProperties();
		return argon.outdated(properties);
	}

	@Override
	public @NotNull Argon2Hash hash(final @NotNull String input,
									final byte @NotNull [] salt,
									final @NotNull HashProperties properties) throws NotArgonHashException {
		Argon2Parameters parameters = Argon2HashMethod.parameters(properties, salt);
		Argon2BytesGenerator generator = new Argon2BytesGenerator();
		generator.init(parameters);

		byte[] hash = new byte[32];
		generator.generateBytes(input.toCharArray(), hash, 0, hash.length);

		return new Argon2Hash(hash, salt, properties);
	}

	@Override
	public Argon2Hash parse(final @NotNull String hash) {
		Hash parsed = Hash.fromString(hash);
		return Argon2Hash.adopt(parsed);
	}

	private static Argon2Parameters parameters(final @NotNull HashProperties properties, final byte @NotNull[] salt) {
		int argonId = properties.getInt("a", Parameter.ARGON2_ID.argon());
		if (argonId <= 0) {
			throw new NotArgonHashException("Cannot create argon2 hash with non-argon hash properties (missing \"a\")");
		}

		int argonVersion = properties.getInt("v", Version.VERSION_13.version());
		if (argonVersion <= 0) {
			throw new NotArgonHashException("Cannot create argon2 hash with non-argon hash properties (missing \"v\")");
		}

		int iterations = properties.getInt("i", Argon2Hash.DEFAULT_ITERATIONS);
		if (iterations <= 0) {
			throw new NotArgonHashException("Cannot create argon2 hash with non-argon hash properties (missing \"i\")");
		}

		int memory = properties.getInt("m", Argon2Hash.DEFAULT_MEMORY);
		if (memory <= 0) {
			throw new NotArgonHashException("Cannot create argon2 hash with non-argon hash properties (missing \"m\")");
		}

		int parallelism = properties.getInt("p", Argon2Hash.DEFAULT_PARALLELISM);
		if (parallelism <= 0) {
			throw new NotArgonHashException("Cannot create argon2 hash with non-argon hash properties (missing \"p\")");
		}

		return new Argon2Parameters.Builder(argonId)
				.withVersion(argonVersion)
				.withIterations(iterations)
				.withMemoryPowOfTwo(memory)
				.withParallelism(parallelism)
				.withSalt(salt)
				.build();
	}
}
