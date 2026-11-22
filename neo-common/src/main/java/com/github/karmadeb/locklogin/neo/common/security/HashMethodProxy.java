package com.github.karmadeb.locklogin.neo.common.security;

import com.github.karmadeb.locklogin.neo.api.Constants;
import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.security.HashMethod;
import com.github.karmadeb.locklogin.neo.api.security.hash.Hash;
import com.github.karmadeb.locklogin.neo.api.security.hash.HashProperties;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused")
public final class HashMethodProxy implements HashMethod {

	private final HashMethod method;

	@Contract("null -> fail")
	public HashMethodProxy(final HashMethod method) {
		this.method = Objects.requireNonNull(method);
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
		String name = properties.getString(Constants.HASH_PROP_RESOURCE_ID);

		return name.equals(this.id().resource()) &&
				this.method.requiresUpdate(hash);
	}

	@Override
	public @NotNull Hash hash(@NotNull String input, byte @NotNull [] salt, @NotNull HashProperties properties) {
		HashProperties clone = HashProperties.cloneOf(Objects.requireNonNull(properties));

		clone.set(Constants.HASH_PROP_NAMESPACE_ID, Constants.LOCKLOGIN);
		clone.set(Constants.HASH_PROP_RESOURCE_ID, this.id().toString());

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
		String name = properties.getString(Constants.HASH_PROP_RESOURCE_ID);

		return name.equals(this.id().resource()) &&
				this.method.verify(hash, input);
	}
}
