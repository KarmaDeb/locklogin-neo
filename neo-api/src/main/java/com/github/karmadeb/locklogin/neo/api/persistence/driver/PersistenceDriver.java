package com.github.karmadeb.locklogin.neo.api.persistence.driver;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.persistence.exception.UnsupportedDriverException;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.util.concurrent.CompletableFuture;

/**
 * The persistence driver allows for any extension to register
 * its own storage method. By default, LockLogin supports
 * - PostgreSQL
 * - MariaDB
 * - MySQL
 * - SQLite
 */
@SuppressWarnings("unused")
public interface PersistenceDriver {

	/**
	 * Get the driver id
	 * @return the name of the driver
	 */
	@NotNull
	NamespacedIdentity id();

	/**
	 * Get the driver name
	 * @return the name of the driver
	 */
	@NotNull
	default String name() {
		return this.id().resource();
	}

	/**
	 * Get the driver provider
	 * @return the provider
	 */
	@NotNull
	Class<?> provider();

	/**
	 * Connects to the database. By nature, this method
	 * MUST be thread-safe
	 * @return the database data source
	 */
	@NotNull
	CompletableFuture<? extends DataSource> connect();

	/**
	 * Get if the driver is currently connected
	 * @return if the driver is connected
	 */
	boolean connected();

	/**
	 * Disconnects from the database driver
	 * @return the disconnect task
	 */
	@NotNull
	CompletableFuture<Void> disconnect();

	/**
	 * Migrate from a different driver to the
	 * current one. Usually when this operation fails,
	 * the plugin tries the reverse by doing {@link #migrateTo(PersistenceDriver)}
	 * @param driver the driver to migrate from
	 * @throws UnsupportedDriverException if the driver is not supported
	 */
	default void migrateFrom(final @NotNull PersistenceDriver driver) throws UnsupportedDriverException {
		throw new UnsupportedDriverException(driver);
	}

	/**
	 * Migrate to a different driver from the
	 * current one. Custom driver implementations should
	 * always add support for this -> default SQL drivers
	 * @param driver the driver to migrate to
	 * @throws UnsupportedDriverException if the driver is not supported
	 */
	void migrateTo(final @NotNull PersistenceDriver driver) throws UnsupportedDriverException;
}
