package com.github.karmadeb.locklogin.neo.api.persistence.driver;

import com.github.karmadeb.locklogin.neo.api.SneakyConsumer;
import com.github.karmadeb.locklogin.neo.api.SneakyFunction;
import com.github.karmadeb.locklogin.neo.api.extension.registry.RegistryItem;
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
 * @param <T> the persistence driver connection kind
 */
public interface PersistenceDriver<C, T> extends RegistryItem {

	enum FormatTarget {
		/**
		 * Format target for a table name
		 */
		TABLE_NAME,
		/**
		 * Format target for a field name
		 */
		FIELD_NAME
	}

	/**
	 * Get the driver name
	 * @return the name of the driver
	 */
	@NotNull
	default String name() {
		return this.id().resource();
	}

	/**
	 * Formats a name depending on the driver. This
	 * is useful for special tables/fields which are also
	 * a reserved driver keyword.
	 * @param name the name to format
	 * @param target the format target
	 * @return the formatted value
	 */
    @NotNull
	String format(final @NotNull String name, final @NotNull FormatTarget target);

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
	CompletableFuture<DataSource> connect();

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
	 * Execute a query
	 * @param function the function to execute
	 * @return the query result task
	 */
	@NotNull
	CompletableFuture<T> query(final @NotNull SneakyFunction<C, T> function);

	/**
	 * Execute a database operation
	 * @param consumer the operation consumer
	 * @return the task
	 */
	@NotNull
	default CompletableFuture<Void> execute(final @NotNull SneakyConsumer<C> consumer) {
		CompletableFuture<Void> wrapper = new CompletableFuture<>();

		query(p -> {
			consumer.accept(p);
			return null;
		}).thenAccept(_ -> wrapper.complete(null)).exceptionallyCompose(err -> {
			wrapper.completeExceptionally(err);
			return wrapper;
		});

		return wrapper;
	}

	/**
	 * Migrate from a different driver to the
	 * current one. Usually when this operation fails,
	 * the plugin tries the reverse by doing {@link #migrateTo(PersistenceDriver)}
	 * @param driver the driver to migrate from
	 * @throws UnsupportedDriverException if the driver is not supported
	 */
	default void migrateFrom(final @NotNull PersistenceDriver<?, ?> driver) throws UnsupportedDriverException {
		throw new UnsupportedDriverException(driver);
	}

	/**
	 * Migrate to a different driver from the
	 * current one. Custom driver implementations should
	 * always add support for this -> default SQL drivers
	 * @param driver the driver to migrate to
	 * @throws UnsupportedDriverException if the driver is not supported
	 */
	void migrateTo(final @NotNull PersistenceDriver<?, ?> driver) throws UnsupportedDriverException;
}
