package com.github.karmadeb.locklogin.neo.api;

import com.github.karmadeb.locklogin.neo.api.agnostic.ServerPlatform;
import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigRegistry;
import com.github.karmadeb.locklogin.neo.api.persistence.PersistenceManager;
import com.github.karmadeb.locklogin.neo.api.persistence.driver.PersistenceDriver;
import com.github.karmadeb.locklogin.neo.api.security.HashManager;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayerManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents the LockLogin API instance
 * @param <T> the platform player type
 */
@SuppressWarnings("unused")
public abstract class LockLogin<T> {

	private static LockLogin<?> instance;

	/**
	 * Represents the different stages LockLogin
	 * has. Each stage locks/unlocks a different subset
	 * of features
	 */
	public enum Stage {
		/**
		 * Configuration stage, this is the only stage in where
		 * the complete subset of API method is available. During
		 * this stage, the plugin also loads all the extensions in
		 * order for them to register their configurations.
		 */
		CONFIGURATION,
		/**
		 * Drivers stage, during this stage, configuration is already
		 * locked and no further modifications are allowed. At this
		 * point is the only one in where the {@link LockLogin#persistence(PersistenceManager)}
		 * persistence configuration is allowed
		 */
		DRIVERS,
		/**
		 * Loaded stage, at this point only the non-critical API methods
		 * are allowed, such as auth layers registry, hash method registration
		 * etc. Updating of persistence manager nor registering new configuration
		 * parts will be ignored operations
		 */
		LOADED
	}

	protected Stage stage = Stage.CONFIGURATION;

	/**
	 * Get the current locklogin stage
	 * @return the current plugin stage
	 */
	@NotNull
	public Stage stage() {
		return this.stage;
	}

	/**
	 * Updates the current stage. This method
	 * is package-private as it's intended to be
	 * called by LockLogin itself. Author will not take
	 * responsibility over any third-party agent modifying
	 * this value and causing issues
	 * @param stage the current plugin stage
	 */
	@Contract("null -> fail")
	void stage(final Stage stage) {
		this.stage = Objects.requireNonNull(stage);
	}

	/**
	 * Get the current platform
	 * @return the current server platform
	 */
	@NotNull
	public abstract ServerPlatform platform();

	/**
	 * Get the extensions configuration registry
	 * @return the configuration registry
	 */
	@NotNull
	public abstract ConfigRegistry configurations();

	/**
	 * Get the current persistence manager
	 * @return the persistence manager
	 */
	@NotNull
	public abstract PersistenceManager<T> persistence();

	/**
	 * Get the current hash manager
	 * @return the hash manager
	 */
	@NotNull
	public abstract HashManager hashes();

	/**
	 * Get the current auth layer manager
	 * @return the auth layer manager
	 */
	@NotNull
	public abstract AuthLayerManager authLayers();

	/**
	 * Set the current persistence manager. This operation
	 * fails if the current stage is not {@link Stage#DRIVERS} or
	 * {@link Stage#CONFIGURATION}, it can also fail if the underlying
	 * {@link PersistenceDriver} is {@link PersistenceDriver#connected()}.
	 * This is also why some drivers might choose to early return true
	 * on this method as soon as a connection is attempted
	 * @param persistence the persistence manager
	 */
	public abstract void persistence(final @NotNull PersistenceManager<T> persistence);

	/**
	 * Register the current instance as the only
	 * LockLogin instance
	 */
	protected void register() {
		if (LockLogin.instance != null) {
			return;
		}

		LockLogin.instance = this;
	}

	/**
	 * Get the current LockLogin instance
	 * @return the LockLogin instance
	 */
	public static LockLogin<?> get() {
		return LockLogin.instance;
	}
}
