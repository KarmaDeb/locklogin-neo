package com.github.karmadeb.locklogin.neo.api.persistence;

import com.github.karmadeb.locklogin.neo.api.persistence.driver.PersistenceDriver;
import com.github.karmadeb.locklogin.neo.api.user.UserManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The persistence manager
 * @param <T> the player type
 */
public abstract class PersistenceManager<T> {

	protected final PersistenceDriver<?, ?> driver;

	/**
	 * Create a new persistence manager
	 * @param driver the driver
	 */
	@Contract("null -> fail")
	public PersistenceManager(final PersistenceDriver<?, ?> driver) {
		this.driver = Objects.requireNonNull(driver);
	}

	/**
	 * Get the user manager
	 * @return the user manager
	 */
	@NotNull
	public abstract UserManager<T> users();
}
