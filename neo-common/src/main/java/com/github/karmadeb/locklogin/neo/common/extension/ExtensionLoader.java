package com.github.karmadeb.locklogin.neo.common.extension;

import com.github.karmadeb.locklogin.neo.api.extension.ExtensionLifecycle;
import com.github.karmadeb.locklogin.neo.api.extension.NeoExtension;
import com.github.karmadeb.locklogin.neo.api.extension.description.ExtensionDescription;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Final implementation? It's the most comfy way I've found
// to actually do this, even though it still looks bad, I think it's
// what most platforms do already, so I might be wrong doing this, but
// also the entire world is
public final class ExtensionLoader {

	private final Map<String, ExtensionProxy> extensions = new ConcurrentHashMap<>();

	public NeoExtension load(final ExtensionLifecycle cycle,
							 final NeoExtension extension,
							 final ExtensionDescription description) {
		String id = description.id();
		if (this.extensions.containsKey(id)) {
			return this.extensions.get(id);
		}

		ExtensionProxy proxy = new ExtensionProxy(extension, description);
		proxy.init(description);
		proxy.loaded(cycle);

		this.extensions.put(description.id(), proxy);
		return proxy;
	}

	public void enable(final ExtensionLifecycle cycle, final NeoExtension extension) {
		ExtensionDescription description = extension.description();
		String id = description.id();

		if (!this.extensions.containsKey(id)) {
			return;
		}

		ExtensionProxy proxy = this.extensions.get(id);
		if (proxy.isEnabled()) {
			return;
		}

		proxy.setEnabled(true);
		proxy.enabled(cycle);
	}

	public void disable(final ExtensionLifecycle cycle, final NeoExtension extension) {
		ExtensionDescription description = extension.description();
		String id = description.id();

		if (!this.extensions.containsKey(id)) {
			return;
		}

		ExtensionProxy proxy = this.extensions.get(id);
		if (!proxy.isEnabled()) {
			return;
		}

		proxy.setEnabled(false);
		proxy.disabled(cycle);
	}

	public Optional<ExtensionProxy> extension(final NeoExtension extension) {
		return this.extension(extension.description());
	}

	public Optional<ExtensionProxy> extension(final ExtensionDescription description) {
		return this.extension(description.id());
	}

	public Optional<ExtensionProxy> extension(final String id) {
		return Optional.ofNullable(this.extensions.get(id));
	}

	public Set<ExtensionProxy> proxies() {
		return new HashSet<>(this.extensions.values());
	}

	public Set<NeoExtension> extensions() {
		return this.extensions(_ -> true);
	}

	public Set<NeoExtension> extensions(final Predicate<NeoExtension> filter) {
		return this.extensions
				.values()
				.stream()
				.map(ExtensionProxy::extension)
				.filter(filter)
				.collect(Collectors.toSet());
	}
}
