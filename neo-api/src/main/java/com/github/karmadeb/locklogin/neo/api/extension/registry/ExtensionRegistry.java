package com.github.karmadeb.locklogin.neo.api.extension.registry;

import com.github.karmadeb.locklogin.neo.api.extension.Extension;
import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.security.HashMethod;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer;
import com.github.karmadeb.locklogin.neo.api.security.hash.Hash;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * The extension registry allows for extensions to register
 * specifications on their name. This is the only way for extensions
 * to actually extend LockLogin functionality
 */
public interface ExtensionRegistry extends Iterable<ExtensionRegistryType<?>> {

	/**
	 * Get the registry extension
	 * @return the extension
	 */
	@NotNull
	Extension extension();

	/**
	 * Get an extension registry type by a type
	 * @param id the registry id
	 * @return the extension registry
	 * @param <T> the registry item type
	 */
	@NotNull
	default <T extends RegistryItem> Optional<ExtensionRegistryType<? super T>> get(final @NotNull NamespacedIdentity id) {
		Objects.requireNonNull(id);
		ExtensionRegistryType<?> kind;

		ExtensionRegistryType<HashMethod> hashes = this.hash();
		ExtensionRegistryType<AuthLayer<?>> layers = this.authLayer();

		if (id.equals(hashes.id())) {
			kind = hashes;
		} else if (id.equals(layers.id())) {
			kind = layers;
		} else {
			return Optional.empty();
		}

		try {
			@SuppressWarnings("unchecked")
			ExtensionRegistryType<T> type = (ExtensionRegistryType<T>) kind;
			return Optional.of(type);
		} catch (ClassCastException ex) {
			return Optional.empty();
		}
	}

	/**
	 * Get the hashes registry
	 * @return the registry
	 */
	@NotNull
	ExtensionRegistryType<HashMethod> hash();

	/**
	 * Get the auth layers registry
	 * @return the auth layer registry
	 */
	@NotNull
	ExtensionRegistryType<AuthLayer<?>> authLayer();

	/**
	 * Get all the registry IDs. The collection
	 * cannot be modified
	 * @return the registry IDs
	 */
	@NotNull @Unmodifiable
	Collection<NamespacedIdentity> registries();

	/**
	 * Get all the globally registered resources
	 * @return the resource keys
	 */
	@NotNull @Unmodifiable
	Collection<NamespacedIdentity> keys();

	/**
	 * Tries to occupy an ID
	 * @param id the ID to occupy
	 * @return if the operation succeed
	 */
	@Contract("null -> true")
	@Internal
	boolean occupy(final NamespacedIdentity id);
}