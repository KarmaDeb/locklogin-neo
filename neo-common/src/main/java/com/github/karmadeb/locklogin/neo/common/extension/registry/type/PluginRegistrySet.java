package com.github.karmadeb.locklogin.neo.common.extension.registry.type;

import com.github.karmadeb.locklogin.neo.api.Constants;
import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.extension.registry.ExtensionRegistryType;
import com.github.karmadeb.locklogin.neo.api.extension.registry.RegistryItem;
import com.github.karmadeb.locklogin.neo.common.extension.NeoRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class PluginRegistrySet<T extends RegistryItem> extends ExtensionRegistryType<T> {

	private final Class<? super T> type;
	private final NamespacedIdentity id;

	/**
	 * Create a new extension registry
	 *
	 * @param registry the registry
	 * @param type the registry type
	 * @param resource the registry resource
	 */
	@Contract("null, _, _ -> fail; _, null, _ -> fail; _, _, null -> fail")
	public PluginRegistrySet(final NeoRegistry registry, final Class<? super T> type, final String resource) {
		super(registry);
		this.type = Objects.requireNonNull(type);
		this.id = NamespacedIdentity.create(Constants.LOCKLOGIN_EXTENSION_ID, resource);
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
	 * @throws Conflict if there's any conflict
	 */
	@Override
	public void register(final T element) throws Conflict {
		if (element.id().namespace().equals(Constants.LOCKLOGIN_EXTENSION_ID) && this.elements.containsKey(element.id())) {
			T registered = this.elements.get(element.id());
			throw new Conflict(element.id(), registered, element);
		}

		NamespacedIdentity identity = NamespacedIdentity.create(Constants.LOCKLOGIN_EXTENSION_ID, this.id.resource());
		ExtensionRegistryType<? super T> registry = NeoRegistry.registry().get(identity).orElseThrow();

		super.register(element);
		registry.register(element);
	}
}
