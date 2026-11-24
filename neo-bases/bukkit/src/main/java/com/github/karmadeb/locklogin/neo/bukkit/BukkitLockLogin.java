package com.github.karmadeb.locklogin.neo.bukkit;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.agnostic.ServerPlatform;
import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigRegistry;
import com.github.karmadeb.locklogin.neo.api.i18n.registry.LocaleRegistry;
import com.github.karmadeb.locklogin.neo.api.persistence.PersistenceManager;
import com.github.karmadeb.locklogin.neo.api.persistence.driver.PersistenceDriver;
import com.github.karmadeb.locklogin.neo.api.security.HashManager;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayerManager;
import com.github.karmadeb.locklogin.neo.common.NeoServerPlatform;
import com.github.karmadeb.locklogin.neo.common.config.registry.ConfigurationRegistry;
import com.github.karmadeb.locklogin.neo.common.i18n.LocalizationRegistry;
import com.github.karmadeb.locklogin.neo.common.security.NeoHashMethodManager;
import com.github.karmadeb.locklogin.neo.common.security.authentication.NeoAuthLayerManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class BukkitLockLogin extends LockLogin<Player> {

	private final String platform;
	private final ConfigurationRegistry configs = new ConfigurationRegistry();
	private final LocalizationRegistry locales = new LocalizationRegistry();
	private final NeoHashMethodManager hashes = new NeoHashMethodManager();
	private final NeoAuthLayerManager layers = new NeoAuthLayerManager();
	private PersistenceManager<Player> persistence;

	public BukkitLockLogin(final String platform) {
		this.platform = Objects.requireNonNull(platform);
	}

	/**
	 * Get the current platform
	 *
	 * @return the current server platform
	 */
	@Override
	public @NotNull ServerPlatform platform() {
		return new NeoServerPlatform(NamespacedIdentity.create("bukkit", this.platform), Player.class);
	}

	/**
	 * Get the configuration registry
	 *
	 * @return the configuration registry
	 */
	@Override
	public @NotNull ConfigurationRegistry configurations() {
		return this.configs;
	}

	/**
	 * Get the localization registry
	 *
	 * @return the localization registry
	 */
	@Override
	public @NotNull LocalizationRegistry localizations() {
		return this.locales;
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
	 * Get the current hash manager
	 *
	 * @return the hash manager
	 */
	@Override
	public @NotNull NeoHashMethodManager hashes() {
		return this.hashes;
	}

	/**
	 * Get the current auth layer manager
	 *
	 * @return the auth layer manager
	 */
	@Override
	public @NotNull NeoAuthLayerManager authLayers() {
		return this.layers;
	}

	/**
	 * Set the current persistence manager. This operation
	 * fails if the current stage is not {@link Stage#DRIVERS} or
	 * {@link Stage#CONFIGURATION}, it can also fail if the underlying
	 * {@link PersistenceDriver} is {@link PersistenceDriver#connected()}.
	 * This is also why some drivers might choose to early return true
	 * on this method as soon as a connection is attempted
	 *
	 * @param persistence the persistence manager
	 */
	@Override
	public void persistence(final @NotNull PersistenceManager<Player> persistence) {
		if (this.stage != Stage.DRIVERS || (this.persistence != null && this.persistence.driver().connected())) {
			return;
		}

		this.persistence = persistence;
	}

	public void setStage(final Stage stage) {
		super.stage(stage);
	}

	public void registerAsProvider() {
		super.register();
	}
}
