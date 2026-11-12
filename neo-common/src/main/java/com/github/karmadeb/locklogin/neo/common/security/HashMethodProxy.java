package com.github.karmadeb.locklogin.neo.common.security;

import com.github.karmadeb.locklogin.neo.api.Constants;
import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.extension.NeoExtension;
import com.github.karmadeb.locklogin.neo.api.security.HashMethod;
import com.github.karmadeb.locklogin.neo.api.security.hash.Hash;
import com.github.karmadeb.locklogin.neo.api.security.hash.HashProperties;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public final class HashMethodProxy implements HashMethod {

	private final NeoExtension extension;
	private final HashMethod method;

	@Contract("_, null -> fail")
	public HashMethodProxy(final NeoExtension extension, final HashMethod method) {
		this.extension = extension;
		this.method = Objects.requireNonNull(method);
	}

	public Optional<NeoExtension> extension() {
		return Optional.ofNullable(this.extension);
	}

	@Override
	public @NotNull NamespacedIdentity id() {
		return Objects.requireNonNull(this.method.id());
	}

	@Override
	public @NotNull HashProperties computeProperties() {
		return Objects.requireNonNull(this.method.computeProperties());
	}

	/**
	 * Check if a hash requires an update based on
	 * its properties. As most hashes does not require
	 * an update after hashing, this method by default
	 * always returns false. While other algorithms such
	 * as Argon2 algorithm might need to overwrite this behavior
	 *
	 * @param hash the hash
	 * @return if the hash is outdated
	 */
	@Override
	public boolean requiresUpdate(final @NotNull Hash hash) {
		HashProperties properties = hash.properties();
		String name = properties.getString(Constants.HASH_PROP_HASH_ID);

		return name.equals(this.id().resource()) &&
				this.method.requiresUpdate(hash);
	}

	@Override
	public @NotNull Hash hash(@NotNull String input, byte @NotNull [] salt, @NotNull HashProperties properties) {
		HashProperties clone = HashProperties.cloneOf(Objects.requireNonNull(properties));

		clone.set(Constants.HASH_PROP_EXT_ID,
				this.extension == null ? Constants.LOCKLOGIN_EXTENSION_ID : this.extension.id());
		clone.set(Constants.HASH_PROP_HASH_ID, this.id().toString());

		return this.method.hash(Objects.requireNonNull(input),
				Objects.requireNonNull(salt),
				clone);
	}

	@Override
	public Hash parse(@NotNull String hash) {
		return this.method.parse(Objects.requireNonNull(hash));
	}

	/**
	 * Verify the hash with the input
	 *
	 * @param hash  the hash
	 * @param input the input
	 * @return if the hashes match
	 */
	@Override
	public boolean verify(@NotNull Hash hash, @NotNull String input) {
		HashProperties properties = hash.properties();
		String name = properties.getString(Constants.HASH_PROP_HASH_ID);

		return name.equals(this.id().resource()) &&
				this.method.verify(hash, input);
	}
}
