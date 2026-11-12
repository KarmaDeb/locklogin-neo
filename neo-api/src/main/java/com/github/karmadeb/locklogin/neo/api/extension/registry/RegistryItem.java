package com.github.karmadeb.locklogin.neo.api.extension.registry;

import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a simple registry item
 */
public interface RegistryItem {

	/**
	 * Get the item id
	 * @return the item id
	 */
	@NotNull NamespacedIdentity id();
}
