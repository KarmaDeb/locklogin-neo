package com.github.karmadeb.locklogin.neo.common.extension;

import com.github.karmadeb.locklogin.neo.api.annotation.TrueAbstract;
import com.github.karmadeb.locklogin.neo.api.extension.ExtensionLifecycle;
import com.github.karmadeb.locklogin.neo.api.extension.NeoExtension;
import com.github.karmadeb.locklogin.neo.api.extension.description.Author;
import com.github.karmadeb.locklogin.neo.api.extension.description.ExtensionDescription;
import com.github.karmadeb.locklogin.neo.api.extension.description.Version;
import com.github.karmadeb.locklogin.neo.api.extension.description.annotation.AuthorMeta;
import com.github.karmadeb.locklogin.neo.api.extension.description.annotation.ExtensionMeta;
import com.github.karmadeb.locklogin.neo.api.extension.description.annotation.VersionMeta;
import com.github.karmadeb.locklogin.neo.api.extension.registry.ExtensionRegistry;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public final class ExtensionProxy extends NeoExtension {

	public static final int FEATURE_LOADED = 1 << 1;
	public static final int FEATURE_ENABLED = 1 << 2;
	public static final int FEATURE_DISABLED = 1 << 3;

	private static final Map<String, Integer> FEATURE_MAP = new HashMap<>(){{
		put("loaded", FEATURE_LOADED);
		put("enabled", FEATURE_ENABLED);
		put("disabled", FEATURE_DISABLED);
	}};

	private static final MethodHandle MH_EXT_INIT;
	private static final MethodHandle MH_EXT_SET_ENABLED;
	private static final MethodHandle MH_EXT_NEW;

	static {
		Class<NeoExtension> extensionClass = NeoExtension.class;

		try {
			Method init = extensionClass.getDeclaredMethod("init", ExtensionDescription.class, ExtensionRegistry.class);
			Method setEnabled = extensionClass.getDeclaredMethod("setEnabled", boolean.class);
			Constructor<? extends NeoExtension> constructor = extensionClass.getConstructor();

			MethodHandles.Lookup lookup = MethodHandles.lookup();

			MH_EXT_INIT = lookup.unreflect(init);
			MH_EXT_SET_ENABLED = lookup.unreflect(setEnabled);
			MH_EXT_NEW = lookup.unreflectConstructor(constructor);
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}

	private final Class<? extends NeoExtension> extension;
	private final ExtensionDescription description;
	private final NeoExtension instance;

	private BigInteger features = BigInteger.ZERO;

	public ExtensionProxy(final Class<? extends NeoExtension> extension) {
		this.extension = extension;
		this.description = loadDescription();
		this.instance = instantiate();

		this.scanFeatures();
		invoke(MH_EXT_INIT, this, description, NeoRegistry.registry().extension(this.instance));
	}

	public NeoExtension extension() {
		return this.instance;
	}

	public void init() {
		invoke(MH_EXT_INIT, this.extension, description, NeoRegistry.registry().extension(this.instance));
	}

	public void setEnabled(final boolean status) {
		invoke(MH_EXT_SET_ENABLED, this.extension, status);
		invoke(MH_EXT_SET_ENABLED, this, status);
	}

	/**
	 * Logic to run after an extension has been loaded. This does not
	 * necessarily mean the extension is currently enabled, but just that
	 * LockLogin recognized it as an extension
	 *
	 * @param lifecycle the lifecycle involved in the operation
	 */
	@Override
	public void loaded(final @NotNull ExtensionLifecycle lifecycle) {
		if (this.features.testBit(FEATURE_LOADED)) {
			try {
				this.instance.loaded(lifecycle);
			} catch (Throwable ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * Logic to run after an extension has been enabled
	 *
	 * @param lifecycle the lifecycle involved in the operation
	 */
	@Override
	public void enabled(final @NotNull ExtensionLifecycle lifecycle) {
		if (this.features.testBit(FEATURE_ENABLED)) {
			try {
				this.instance.enabled(lifecycle);
			} catch (Throwable ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * Logic to run after an extension has been disabled
	 *
	 * @param lifecycle the lifecycle involved in the operation
	 */
	@Override
	public void disabled(final @NotNull ExtensionLifecycle lifecycle) {
		if (this.features.testBit(FEATURE_DISABLED)) {
			try {
				this.instance.disabled(lifecycle);
			} catch (Throwable ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private ExtensionDescription loadDescription() {
		if (!this.extension.isAnnotationPresent(ExtensionMeta.class)) {
			throw new IllegalStateException(String.format("Failed to load extension %s, missing @ExtensionMeta",
					this.extension.getCanonicalName()));
		}

		ExtensionMeta meta = this.extension.getAnnotation(ExtensionMeta.class);
		LinkedHashSet<Author> authors = loadAuthors(meta);
		Version version = loadVersion(meta.version());

		return new ExtensionDescription(meta.id(),
				meta.name(),
				meta.description(),
				version,
				Collections.unmodifiableSequencedSet(authors),
				Collections.unmodifiableSequencedSet(new LinkedHashSet<>(
						Arrays.asList(meta.dependsOn())
				)),
				Collections.unmodifiableSequencedSet(new LinkedHashSet<>(
						Arrays.asList(meta.runBefore())
				)));
	}

	private static @NotNull Version loadVersion(VersionMeta versionMeta) {
		Version version;
		if (versionMeta.patch() >= 0 && !versionMeta.data().isBlank()) {
			version = new Version(versionMeta.major(), versionMeta.minor(), versionMeta.patch(), versionMeta.data());
		} else if (versionMeta.patch() >= 0) {
			version = new Version(versionMeta.major(), versionMeta.minor(), versionMeta.patch());
		} else if (!versionMeta.data().isBlank()) {
			version = new Version(versionMeta.major(), versionMeta.minor(), versionMeta.data());
		} else {
			version = new Version(versionMeta.major(), versionMeta.minor());
		}
		return version;
	}

	private static @NotNull LinkedHashSet<Author> loadAuthors(ExtensionMeta meta) {
		AuthorMeta[] authorMetas = meta.authors();

		if (authorMetas == null || authorMetas.length == 0) {
			throw new IllegalStateException(String.format("Failed to load extension %s, required at least 1 author",
					meta.id()));
		}

		LinkedHashSet<Author> authors = new LinkedHashSet<>();
		for (AuthorMeta author : authorMetas) {
			Optional<String> email = author.address().isBlank() ? Optional.empty() : Optional.of(author.address());
			Optional<URI> web;
			try {
				if (author.website().isBlank()) {
					web = Optional.empty();
				} else {
					URI uri = new URI(author.website());
					web = Optional.of(uri);
				}
			} catch (URISyntaxException _) {
				web = Optional.empty();
			}

			authors.add(new Author(author.username(), email, web));
		}
		return authors;
	}

	private NeoExtension instantiate() {
		try {
			Constructor<? extends NeoExtension> constructor = this.extension.getDeclaredConstructor(ExtensionDescription.class);
			return constructor.newInstance(this.description);
		} catch (NoSuchMethodException _) {
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		try {
			return (NeoExtension) MH_EXT_NEW.invoke();
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}

	private void scanFeatures() {
		for (Method method : NeoExtension.class.getMethods()) {
			if (!method.isAnnotationPresent(TrueAbstract.class)) {
				continue;
			}
			String feature = method.getName();
			if (!FEATURE_MAP.containsKey(feature)) {
				continue;
			}

			int featureBit = FEATURE_MAP.get(feature);

			try {
				this.extension.getDeclaredMethod(method.getName(), method.getParameterTypes());
				this.features = this.features.setBit(featureBit);
			} catch (NoSuchMethodException ignored) {}
		}
	}

	private void invoke(final MethodHandle handle, final Object... args) {
		try {
			MethodHandle bind = handle.bindTo(extension);
			bind.invoke(args);
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}
}
