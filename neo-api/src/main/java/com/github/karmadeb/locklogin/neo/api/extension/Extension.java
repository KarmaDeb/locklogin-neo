package com.github.karmadeb.locklogin.neo.api.extension;

import com.github.karmadeb.locklogin.neo.api.extension.description.Author;
import com.github.karmadeb.locklogin.neo.api.extension.description.ExtensionDescription;
import com.github.karmadeb.locklogin.neo.api.extension.description.Version;
import com.github.karmadeb.locklogin.neo.api.extension.registry.ExtensionRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;
import java.util.Objects;


/**
 * NeoLockLogin extension
 */
public interface Extension {

	/**
	 * Get the extension description
	 * @return the description
	 */
	ExtensionDescription description();

	/**
	 * Get the extension registry
	 * @return the extension registry
	 */
	ExtensionRegistry registry();

	/**
	 * Generate a resource ID for the specified
	 * resource name
	 * @param resource the resource
	 * @return the extension resource
	 */
	@Contract("null -> fail")
	@NotNull NamespacedIdentity id(final String resource);

	/**
	 * Get the extension ID
	 * @return the extension ID
	 */
	@NotNull String id();

	/**
	 * Get the extension name
	 * @return the extension name
	 */
	@NotNull String name();

	/**
	 * Get the extension version
	 * @return the extension version
	 */
	@NotNull Version version();

	/**
	 * Get the extension author
	 * @return the authors
	 */
	Author @NotNull[] author();

	/**
	 * Logic to run after an extension has been loaded. This does not
	 * necessarily mean the extension is currently enabled, but just that
	 * LockLogin recognized it as an extension
	 * @param lifecycle the lifecycle involved in the operation
	 */
	void loaded(final @NotNull ExtensionLifecycle lifecycle);

	/**
	 * Logic to run after an extension has been enabled
	 * @param lifecycle the lifecycle involved in the operation
	 */
	void enabled(final @NotNull ExtensionLifecycle lifecycle);

	/**
	 * Logic to run after an extension has been disabled
	 * @param lifecycle the lifecycle involved in the operation
	 */
	void disabled(final @NotNull ExtensionLifecycle lifecycle);

	/**
	 * Get if the extension is currently
	 * enabled
	 * @return if the extension is enabled
	 */
	boolean isEnabled();
}
