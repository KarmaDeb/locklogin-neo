package com.github.karmadeb.locklogin.neo.common.extension;

import com.github.karmadeb.locklogin.neo.api.extension.Extension;
import com.github.karmadeb.locklogin.neo.api.extension.ExtensionLifecycle;
import com.github.karmadeb.locklogin.neo.api.extension.ExtensionManager;
import com.github.karmadeb.locklogin.neo.api.extension.NeoExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;


public final class ExtensionLoader implements ExtensionManager {

	private final Map<String, NeoExtension> extensions = new ConcurrentHashMap<>();
	private final Map<Class<? extends Extension>, ExtensionProxy> proxies = new ConcurrentHashMap<>();

	private static ExtensionLifecycle lifecycle = ExtensionLifecycle.PLUGIN_LOAD;

	public static void setLifecycle(final ExtensionLifecycle cycle) {
		lifecycle = cycle;
	}

	public static ExtensionLifecycle getLifecycle() {
		return lifecycle;
	}

	/**
	 * Tries to load an extension.
	 *
	 * @param extension the extension to load
	 * @return the loaded extension
	 */
	@Override
	public Extension load(final @NotNull Class<? extends NeoExtension> extension) {
		ExtensionProxy proxy = this.proxies.get(extension);
		if (proxy != null) {
			return proxy.extension();
		}

		proxy = new ExtensionProxy(extension);
		proxy.init();

		this.extensions.put(proxy.id(), proxy.extension());
		this.proxies.put(extension, proxy);

		proxy.loaded(lifecycle);

		return proxy.extension();
	}

	/**
	 * Enables an extension
	 *
	 * @param extension the extension to enable
	 * @return if the extension was enabled
	 */
	@Override
	public boolean enable(final @NotNull Extension extension) {
		Class<? extends Extension> ext = extension.getClass();
		ExtensionProxy proxy = this.proxies.get(ext);
		if (proxy == null || proxy.isEnabled()) {
			return false;
		}

		proxy.setEnabled(true);
		proxy.enabled(lifecycle);

		return true;
	}

	/**
	 * Disables an extension
	 *
	 * @param extension the extension to disable
	 * @return if the extension was disabled
	 */
	@Override
	public boolean disable(final @NotNull Extension extension) {
		Class<? extends Extension> ext = extension.getClass();
		ExtensionProxy proxy = this.proxies.get(ext);
		if (proxy == null || !proxy.isEnabled()) {
			return false;
		}

		proxy.setEnabled(false);
		proxy.disabled(lifecycle);

		return true;
	}

	/**
	 * Get an extension by its ID
	 *
	 * @param id the extension ID
	 * @return the extension
	 */
	@Override
	public Optional<Extension> extension(final @NotNull String id) {
		return Optional.ofNullable(this.extensions.get(id));
	}

	/**
	 * Get all the registered extensions
	 *
	 * @param filter the extension filter
	 * @return the extensions
	 */
	@Override @SuppressWarnings({"unchecked", "rawtypes"})
	public @NotNull @Unmodifiable Collection<? extends Extension> extensions(final @NotNull Predicate<? extends Extension> filter) {
		return this.extensions.values()
				.stream()
				.filter(ext -> ((Predicate) filter).test(ext))
				.toList();
	}
}
