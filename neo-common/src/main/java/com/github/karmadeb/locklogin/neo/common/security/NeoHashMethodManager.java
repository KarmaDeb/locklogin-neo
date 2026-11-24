package com.github.karmadeb.locklogin.neo.common.security;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.security.HashManager;
import com.github.karmadeb.locklogin.neo.api.security.HashMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class NeoHashMethodManager implements HashManager {

	private final Map<Purpose, Set<HashMethod>> methods = new ConcurrentHashMap<>();

	/**
	 * Register a new hash method. The same hash method can
	 * be registered for different purposes. This method returns
	 * false if another hash method is already registered under
	 * the same {@link HashMethod#id()}
	 *
	 * @param method  the method to register
	 * @param purpose the purpose of the hash
	 */
	@Override
	public boolean register(final @NotNull HashMethod method, final @NotNull Purpose purpose) {
		Set<HashMethod> purposeMethods = methods.computeIfAbsent(purpose, _ -> ConcurrentHashMap.newKeySet());
		if (purposeMethods.stream().anyMatch(m -> m.id().equals(method.id()))) {
			return false;
		}

		purposeMethods.add(method);
		return true;
	}

	/**
	 * Get a hash method. This method returns an empty optional
	 * even if the method with ID exists, but its purpose is not
	 * intended for the provided one
	 *
	 * @param id      the method id
	 * @param purpose the method purpose
	 * @return the hashing method
	 */
	@Override
	public Optional<HashMethod> get(final @NotNull NamespacedIdentity id, final @NotNull Purpose purpose) {
		Set<HashMethod> purposeMethods = methods.computeIfAbsent(purpose, _ -> ConcurrentHashMap.newKeySet());
		return purposeMethods.stream().filter(m -> m.id().equals(id)).findAny();
	}

	/**
	 * Get all the hashing methods for the specified
	 * hashing purpose
	 *
	 * @param purpose the purpose
	 * @return the hashing methods
	 */
	@Override
	public @NotNull @Unmodifiable Collection<HashMethod> collect(final @NotNull Purpose purpose) {
		Set<HashMethod> purposeMethods = methods.computeIfAbsent(purpose, _ -> ConcurrentHashMap.newKeySet());
		return Collections.unmodifiableCollection(purposeMethods);
	}
}
