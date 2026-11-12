package com.github.karmadeb.locklogin.neo.common.extension;

import com.github.karmadeb.locklogin.neo.api.annotation.TrueAbstract;
import com.github.karmadeb.locklogin.neo.api.extension.ExtensionLifecycle;
import com.github.karmadeb.locklogin.neo.api.extension.NeoExtension;
import com.github.karmadeb.locklogin.neo.api.extension.description.ExtensionDescription;
import com.github.karmadeb.locklogin.neo.api.extension.registry.ExtensionRegistry;
import com.github.karmadeb.locklogin.neo.common.registry.NeoRegistry;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

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

	static {
		Class<NeoExtension> extensionClass = NeoExtension.class;

		try {
			Method init = extensionClass.getDeclaredMethod("init", ExtensionDescription.class, ExtensionRegistry.class);
			Method setEnabled = extensionClass.getDeclaredMethod("setEnabled", boolean.class);

			MethodHandles.Lookup lookup = MethodHandles.lookup();

			MH_EXT_INIT = lookup.unreflect(init);
			MH_EXT_SET_ENABLED = lookup.unreflect(setEnabled);
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}

	private final NeoExtension extension;
	private BigInteger features = BigInteger.ZERO;

	public ExtensionProxy(final NeoExtension extension, final ExtensionDescription description) {
		this.extension = extension;
		this.scanFeatures();

		invoke(MH_EXT_INIT, this, description, NeoRegistry.registry().extension(this.extension));
	}

	public NeoExtension extension() {
		return this.extension;
	}

	public void init(final ExtensionDescription description) {
		invoke(MH_EXT_INIT, this.extension, description, NeoRegistry.registry().extension(this.extension));
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
			this.extension.loaded(lifecycle);
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
			this.extension.enabled(lifecycle);
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
			this.extension.disabled(lifecycle);
		}
	}

	private void scanFeatures() {
		Class<? extends NeoExtension> ext = this.extension.getClass();
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
				ext.getDeclaredMethod(method.getName(), method.getParameterTypes());
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
