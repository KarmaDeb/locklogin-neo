package com.github.karmadeb.locklogin.neo.common.extension.registry;

import com.github.karmadeb.locklogin.neo.api.Constants;
import com.github.karmadeb.locklogin.neo.api.extension.Extension;
import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.extension.registry.ExtensionRegistry;
import com.github.karmadeb.locklogin.neo.api.extension.registry.ExtensionRegistryType;
import com.github.karmadeb.locklogin.neo.api.security.HashMethod;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer;
import com.github.karmadeb.locklogin.neo.common.extension.registry.type.ExtensionRegistrySet;
import com.github.karmadeb.locklogin.neo.common.registry.NeoRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;


public class NeoExtensionRegistry implements ExtensionRegistry {

    private final Extension extension;
	private final NeoRegistry registry;
	private final ExtensionRegistryType<HashMethod> hash;
	private final ExtensionRegistryType<AuthLayer<?>> layer;

	@Contract("null, _ -> fail; _, null -> fail")
	public NeoExtensionRegistry(final Extension extension, final NeoRegistry registry) {
		this.extension = Objects.requireNonNull(extension);
		this.registry = Objects.requireNonNull(registry);

		this.hash = new ExtensionRegistrySet<>(this, HashMethod.class,
				extension.id(Constants.RESOURCE_HASH));
		this.layer = new ExtensionRegistrySet<>(this, AuthLayer.class,
				extension.id(Constants.RESOURCE_AUTH_LAYER));
	}

	/**
	 * Get the registry extension
	 *
	 * @return the extension
	 */
	@Override
	public @NotNull Extension extension() {
		return this.extension;
	}

	/**
	 * Get the hashes registry
	 *
	 * @return the registry
	 */
	@Override
	public @NotNull ExtensionRegistryType<HashMethod> hash() {
		return this.hash;
	}

	/**
	 * Get the auth layers registry
	 *
	 * @return the auth layer registry
	 */
	@Override
	public @NotNull ExtensionRegistryType<AuthLayer<?>> authLayer() {
		return this.layer;
	}

	/**
	 * Get all the registry IDs. The collection
	 * cannot be modified
	 *
	 * @return the registry IDs
	 */
	@Override
	public @NotNull @Unmodifiable Collection<NamespacedIdentity> registries() {
		Set<NamespacedIdentity> identities = new LinkedHashSet<>();
		identities.addAll(this.hash.keys());
		identities.addAll(this.layer.keys());

		return Collections.unmodifiableCollection(identities);
	}

	/**
	 * Get all the globally registered resources
	 *
	 * @return the resource keys
	 */
	@Override
	public @NotNull @Unmodifiable Collection<NamespacedIdentity> keys() {
    	return this.registry.keys();
	}

	/**
	 * Tries to occupy an ID
	 * @param id the ID to occupy
	 * @return if the operation succeed
	 */
	@Override
	public boolean occupy(final NamespacedIdentity id) {
		if (id == null) {
			return true;
		}

		NamespacedIdentity res = this.extension.id(id.resource());
		if (!res.equals(id)) {
			throw new IllegalStateException(String.format(
					"Element id does not match registry's namespace (%s <> %s)",
					id,
					res
			));
		}

		return this.registry.occupy(id);
	}

	/**
	 * Returns an iterator over elements of type {@code T}.
	 *
	 * @return an Iterator.
	 */
	@Override
	public @NotNull Iterator<ExtensionRegistryType<?>> iterator() {
		return Arrays.asList(this.hash, this.layer).iterator();
	}
}
