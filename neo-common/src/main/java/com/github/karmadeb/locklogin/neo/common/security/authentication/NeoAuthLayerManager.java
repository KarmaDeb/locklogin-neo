package com.github.karmadeb.locklogin.neo.common.security.authentication;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class NeoAuthLayerManager implements AuthLayerManager {

	private final Map<NamespacedIdentity, AuthLayer<?>> layers = new ConcurrentHashMap<>();

	/**
	 * Register a new auth layer. This method returns
	 * false if another auth layer is already registered under
	 * the same {@link AuthLayer#id()}
	 *
	 * @param layer the layer to register
	 */
	@Override
	public boolean register(final @NotNull AuthLayer<?> layer) {
		NamespacedIdentity id = Objects.requireNonNull(layer).id();
		if (this.layers.containsKey(id)) {
			return false;
		}

		this.layers.put(id, layer);
		return true;
	}

	/**
	 * Get an auth layer. This method will return an empty
	 * optional only if the layer has not been globally registered.
	 * Manually registering a layer in a session does not
	 * automatically register it in the global manager
	 *
	 * @param id the layer id
	 * @return the auth layer
	 */
	@Override
	public Optional<AuthLayer<?>> get(final @NotNull NamespacedIdentity id) {
		return Optional.ofNullable(this.layers.get(id));
	}

	/**
	 * Get all the auth layers registered
	 *
	 * @return the layers
	 */
	@Override
	public @NotNull @Unmodifiable Collection<AuthLayer<?>> collect() {
		return Collections.unmodifiableCollection(this.layers.values());
	}
}
