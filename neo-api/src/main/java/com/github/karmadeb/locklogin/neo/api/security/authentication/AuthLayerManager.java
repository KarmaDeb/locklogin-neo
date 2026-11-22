package com.github.karmadeb.locklogin.neo.api.security.authentication;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;

/**
 * The auth layer manager allows to register different mechanism
 * of user authentication that the plugin is cappable of.
 * Once an auth layer is registered, it cannot be unregistered
 * nor overwritten
 */
public interface AuthLayerManager {

	/**
	 * Register a new auth layer. This method returns
	 * false if another auth layer is already registered under
	 * the same {@link AuthLayer#id()}
	 * @param layer the layer to register
	 */
	boolean register(final @NotNull AuthLayer<?> layer);

	/**
	 * Get an auth layer. This method will return an empty
	 * optional only if the layer has not been globally registered.
	 * Manually registering a layer in a session does not
	 * automatically register it in the global manager
	 * @param id the layer id
	 * @return the auth layer
	 */
	Optional<AuthLayer<?>> get(final @NotNull NamespacedIdentity id);

	/**
	 * Get all the auth layers registered
	 * @return the layers
	 */
	@NotNull @Unmodifiable
	Collection<AuthLayer<?>> collect();
}
