package com.github.karmadeb.locklogin.neo.common.extension;


import com.github.karmadeb.locklogin.neo.api.Constants;
import com.github.karmadeb.locklogin.neo.api.extension.Extension;
import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.extension.NeoExtension;
import com.github.karmadeb.locklogin.neo.api.extension.registry.ExtensionRegistry;
import com.github.karmadeb.locklogin.neo.api.extension.registry.ExtensionRegistryType;
import com.github.karmadeb.locklogin.neo.api.security.HashMethod;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer;
import com.github.karmadeb.locklogin.neo.common.extension.registry.NeoExtensionRegistry;
import com.github.karmadeb.locklogin.neo.common.extension.registry.type.PluginRegistrySet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class NeoRegistry implements ExtensionRegistry {

	private static final NeoRegistry registry = new NeoRegistry();

	private final Set<NamespacedIdentity> registered = ConcurrentHashMap.newKeySet();
	private final Map<Extension, NeoExtensionRegistry> registries = new ConcurrentHashMap<>();
	private final ExtensionRegistryType<HashMethod> hash = new PluginRegistrySet<>(this,
			HashMethod.class, Constants.RESOURCE_HASH);
	private final ExtensionRegistryType<AuthLayer<?>> layer = new PluginRegistrySet<>(this,
			AuthLayer.class, Constants.RESOURCE_AUTH_LAYER);

	private NeoRegistry() {}

	/**
	 * Get the registry extension
	 *
	 * @return the extension
	 */
	@Override
	public @NotNull NeoExtension extension() {
		throw new UnsupportedOperationException();
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
		Set<NamespacedIdentity> collector = new LinkedHashSet<>();
		for (NeoExtensionRegistry registry : this.registries.values()) {
			collector.addAll(registry.registries());
		}

		return Collections.unmodifiableCollection(collector);
	}

	/**
	 * Get all the globally registered resources
	 *
	 * @return the resource keys
	 */
	@Override
	public @NotNull @Unmodifiable Collection<NamespacedIdentity> keys() {
		return Collections.unmodifiableCollection(this.registered);
	}

	/**
	 * Tries to occupy an ID
	 *
	 * @param id the ID to occupy
	 * @return if the operation succeed
	 */
	@Override
	public boolean occupy(final NamespacedIdentity id) {
		return this.registered.add(id);
	}

	@Contract("null -> fail")
	public NeoExtensionRegistry extension(final Extension extension) {
		return this.registries.computeIfAbsent(Objects.requireNonNull(extension),
				_ -> new NeoExtensionRegistry(extension, this));
	}

	public static NeoRegistry registry() {
		return NeoRegistry.registry;
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
