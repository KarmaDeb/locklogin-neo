package com.github.karmadeb.locklogin.neo.bukkit;

import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigRegistry;
import com.github.karmadeb.locklogin.neo.api.extension.ExtensionManager;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession;
import com.github.karmadeb.locklogin.neo.common.config.registry.ConfigurationRegistry;
import com.github.karmadeb.locklogin.neo.common.extension.ExtensionLoader;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * LockLogin API helper for bukkit environments
 */
public class LockLogin {

	private static LockLogin instance;

	private final ExtensionManager manager = new ExtensionLoader();
	private final ConfigRegistry config = new ConfigurationRegistry();

	private LockLogin() {}

	/**
	 * Get the extension manager.
	 * @return the extension manager
	 */
	@NotNull
	public ExtensionManager extensions() {
		return this.manager;
	}

	/**
	 * Get the extensions configuration registry
	 * @return the configuration registry
	 */
	@NotNull
	public ConfigRegistry configurations() {
		return this.config;
	}

	/**
	 * Get the LockLogin instance
	 * @return the LockLogin API
	 */
	@NotNull
	public static LockLogin get() {
		return instance;
	}

	protected final void register() {
		if (LockLogin.instance != null) {
			return;
		}

		LockLogin.instance = this;
	}
}
