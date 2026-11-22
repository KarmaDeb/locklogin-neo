package com.github.karmadeb.locklogin.neo.bukkit;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.api.agnostic.ServerPlatform;
import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigRegistry;
import com.github.karmadeb.locklogin.neo.api.extension.ExtensionManager;
import com.github.karmadeb.locklogin.neo.api.persistence.PersistenceManager;
import com.github.karmadeb.locklogin.neo.common.NeoServerPlatform;
import com.github.karmadeb.locklogin.neo.common.config.registry.ConfigurationRegistry;
import com.github.karmadeb.locklogin.neo.common.extension.ExtensionLoader;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * LockLogin API helper for bukkit environments
 */
@SuppressWarnings("unused")
public final class BukkitLockLogin extends LockLogin<Player> {

	private final NeoServerPlatform platform;
	private final ExtensionManager extensions = new ExtensionLoader();
	private final ConfigRegistry config = new ConfigurationRegistry();
	private PersistenceManager<Player> persistence;

	@Contract("null -> fail")
	private BukkitLockLogin(final NeoServerPlatform platform) {
		this.platform = Objects.requireNonNull(platform);
	}

	/**
	 * Get the current platform
	 *
	 * @return the current server platform
	 */
	@Override
	public @NotNull ServerPlatform platform() {
		return this.platform;
	}

	/**
	 * Get the extension manager.
	 *
	 * @return the extension manager
	 */
	@Override
	public @NotNull ExtensionManager extensions() {
		return this.extensions;
	}

	/**
	 * Get the extensions configuration registry
	 *
	 * @return the configuration registry
	 */
	@Override
	public @NotNull ConfigRegistry configurations() {
		return this.config;
	}

	/**
	 * Get the current persistence manager
	 *
	 * @return the persistence manager
	 */
	@Override
	public @NotNull PersistenceManager<Player> persistence() {
		return this.persistence;
	}

	/**
	 * Set the current persistence manager. This operation
	 * fails if the current stage is not {@link Stage#DRIVERS} or
	 * {@link Stage#CONFIGURATION}
	 *
	 * @param persistence the persistence manager
	 */
	@Override
	public void persistence(final @NotNull PersistenceManager<Player> persistence) {
		if (Stage.LOADED.equals(this.stage) || (this.persistence != null &&
				this.persistence.driver().connected())) {
			return;
		}

		this.persistence = Objects.requireNonNull(persistence);
	}

	public static BukkitLockLogin get() {
		return (BukkitLockLogin) LockLogin.get();
	}
}