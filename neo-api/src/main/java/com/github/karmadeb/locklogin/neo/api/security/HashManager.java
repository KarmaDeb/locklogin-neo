package com.github.karmadeb.locklogin.neo.api.security;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;

/**
 * The hash manager allows to register different types
 * of hashing algorithms which the plugin will later use.
 * Once a hash method is registered, it cannot be unregistered
 * nor overwritten
 */
public interface HashManager {

	/**
	 * Hashing method purposes
	 */
	enum Purpose {
		/**
		 * Hash which purpose is to hash passwords
		 */
		PASSWORD,
		/**
		 * Hash which purpose is to provide some method
		 * of verification (i.e: signing)
		 */
		VERIFICATION
	}

	/**
	 * Register a new hash method. The same hash method can
	 * be registered for different purposes. This method returns
	 * false if another hash method is already registered under
	 * the same {@link HashMethod#id()}
	 * @param method the method to register
	 * @param purpose the purpose of the hash
	 */
	boolean register(final @NotNull HashMethod method, final @NotNull Purpose purpose);

	/**
	 * Get a hash method. This method returns an empty optional
	 * even if the method with ID exists, but its purpose is not
	 * intended for the provided one
	 * @param id the method id
	 * @param purpose the method purpose
	 * @return the hashing method
	 */
	Optional<HashMethod> get(final @NotNull NamespacedIdentity id, final @NotNull Purpose purpose);

	/**
	 * Get all the hashing methods for the specified
	 * hashing purpose
	 * @param purpose the purpose
	 * @return the hashing methods
	 */
	@NotNull @Unmodifiable
	Collection<HashMethod> collect(final @NotNull Purpose purpose);
}
