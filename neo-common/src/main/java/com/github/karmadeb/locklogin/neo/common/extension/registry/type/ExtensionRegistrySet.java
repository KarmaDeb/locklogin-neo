package com.github.karmadeb.locklogin.neo.common.extension.registry.type;

import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.extension.registry.ExtensionRegistryType;
import com.github.karmadeb.locklogin.neo.api.extension.registry.RegistryItem;
import com.github.karmadeb.locklogin.neo.common.extension.registry.NeoExtensionRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ExtensionRegistrySet<T extends RegistryItem> extends ExtensionRegistryType<T> {

	private final Class<? super T> type;
	private final NamespacedIdentity id;

	/**
	 * Create a new extension registry
	 *
	 * @param registry the registry
	 * @param type the registry type
	 * @param id the registry id
	 */
	@Contract("null, _, _ -> fail; _, null, _ -> fail; _, _, null -> fail")
	public ExtensionRegistrySet(final NeoExtensionRegistry registry, final Class<? super T> type, final NamespacedIdentity id) {
		super(registry);
		this.type = Objects.requireNonNull(type);
		this.id = Objects.requireNonNull(id);
	}

	/**
	 * Get the registry type
	 *
	 * @return the type
	 */
	@Override @SuppressWarnings("unchecked")
	public @NotNull Class<T> type() {
		return (Class<T>) this.type;
	}

	/**
	 * Get the registry type ID
	 *
	 * @return the ID
	 */
	@Override
	public @NotNull NamespacedIdentity id() {
		return this.id;
	}

	/**
	 * Register a new element
	 *
	 * @param element the element to register
	 */
	@Override
	public void register(final T element) {
		NamespacedIdentity id = element.id();
		T registered = this.elements.get(element.id());
		if (registered != null) {
			NamespacedIdentity origin = registered.id();
			if (!origin.namespace().equals(id.namespace())) {
				return;
			}
		}

		this.elements.put(id, element);
	}
}
