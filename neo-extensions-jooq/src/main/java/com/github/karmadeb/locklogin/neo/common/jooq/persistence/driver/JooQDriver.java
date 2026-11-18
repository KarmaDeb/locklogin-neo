package com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver;

import com.github.karmadeb.locklogin.neo.api.Constants;
import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.persistence.driver.PersistenceDriver;
import com.github.karmadeb.locklogin.neo.api.persistence.exception.UnsupportedDriverException;
import com.github.karmadeb.locklogin.neo.common.jooq.orm.Tables;
import com.github.karmadeb.locklogin.neo.common.jooq.orm.tables.records.UsersRecord;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.conf.WriteIfReadonly;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The JOOQ driver is the official LockLogin method to deal
 * with multiple database engines in a very easy way; dealing
 * the query building to a third-party library.
 * This comes with its own caveats such as limiting the official
 * dialects to the one the library supports, and according to the
 * JOOQ community license:
 * <ul>
 * 		<li>
 * 	    	It is governed under the terms of the Apache Software License 2.0 (ASL 2.0).
 * 		</li>
 *  	<li>
 *      	Free usage is intended and officially supported exclusively for open-source
 *   		database engines (such as MySQL, PostgreSQL, MariaDB, SQLite, H2, etc.).
 *   	</li>
 *  	<li>
 *      	The use of commercial databases (such as Oracle, SQL Server, DB2, etc.) or
 *   		advanced commercial-only features requires a paid commercial license.
 *   	</li>
 *  	<li>
 *      	Redistributions and derivative works must retain the original copyright notices,
 *   		conditions, and a copy of the Apache 2.0 license.
 *   	</li>
 * </ul>
 */
public abstract class JooQDriver implements PersistenceDriver {

	protected static final ExecutorService SQL_ASYNC_EXECUTOR = Executors.newWorkStealingPool();

	protected final Class<?> driver;
	protected final NamespacedIdentity id;
	private final AtomicReference<CompletableFuture<HikariDataSource>> connectTaskRef = new AtomicReference<>();
	private final AtomicReference<CompletableFuture<Void>> closeTaskRef = new AtomicReference<>();

	protected HikariDataSource dataSource;

	/**
	 * Creates a new JOOQ driver
	 * @param driver the driver class
	 * @param dialect the driver dialect
	 */
	@Contract("null, _ -> fail; _, null -> fail")
	public JooQDriver(final Class<?> driver, final SQLDialect dialect) {
		this.driver = Objects.requireNonNull(driver);
		this.id = NamespacedIdentity.create(Constants.LOCKLOGIN_EXTENSION_ID, Objects.requireNonNull(dialect).name());
	}

	/**
	 * Get the item id
	 *
	 * @return the item id
	 */
	@Override
	public final @NotNull NamespacedIdentity id() {
		return this.id;
	}

	/**
	 * Get the driver provider
	 *
	 * @return the provider
	 */
	@Override
	public final @NotNull Class<?> provider() {
		return this.driver;
	}

	/**
	 * Connects to the database
	 *
	 * @return the database data source
	 */
	@Override
	public final @NotNull CompletableFuture<HikariDataSource> connect() {
		if (this.dataSource != null && !this.dataSource.isClosed()) {
			return CompletableFuture.completedFuture(this.dataSource);
		}

		CompletableFuture<HikariDataSource> existing = connectTaskRef.get();
		if (existing != null && !existing.isCompletedExceptionally() && !existing.isCancelled()) {
			return existing;
		}

		CompletableFuture<HikariDataSource> newTask = new CompletableFuture<>();
		while (true) {
			existing = connectTaskRef.get();

			if (existing != null && !existing.isCompletedExceptionally() && !existing.isCancelled()) {
				return existing;
			}

			if (connectTaskRef.compareAndSet(existing, newTask)) {
				SQL_ASYNC_EXECUTOR.submit(() -> {
					try {
						HikariDataSource source = configure();
						this.dataSource = source;
						newTask.complete(source);
					} catch (Throwable ex) {
						newTask.completeExceptionally(ex);
					}
				});
				return newTask;
			}
		}
	}

	/**
	 * Get if the driver is currently connected
	 *
	 * @return if the driver is connected
	 */
	@Override
	public final boolean connected() {
		return this.dataSource != null && !this.dataSource.isClosed();
	}

	/**
	 * Disconnects from the database driver
	 *
	 * @return the disconnect task
	 */
	@Override
	public final @NotNull CompletableFuture<Void> disconnect() {
		if (this.dataSource == null) {
			return CompletableFuture.completedFuture(null);
		}

		CompletableFuture<Void> existing = closeTaskRef.get();
		if (existing != null && !existing.isCompletedExceptionally() && !existing.isCancelled()) {
			return existing;
		}

		CompletableFuture<Void> newTask = new CompletableFuture<>();
		while (true) {
			existing = closeTaskRef.get();

			if (existing != null && !existing.isCompletedExceptionally() && !existing.isCancelled()) {
				return existing;
			}

			if (closeTaskRef.compareAndSet(existing, newTask)) {
				SQL_ASYNC_EXECUTOR.submit(() -> {
					try {
						if (!this.dataSource.isClosed()) {
							this.dataSource.close();
						}

						this.dataSource = null;
					} catch (Throwable ex) {
						newTask.completeExceptionally(ex);
					}
				});

				return newTask;
			}
		}
	}

	@NotNull
	protected abstract HikariDataSource configure();

	/**
	 * Migrate to a different driver from the
	 * current one. Custom driver implementations should
	 * always add support for this -> default SQL drivers
	 *
	 * @param driver the driver to migrate to
	 * @throws UnsupportedDriverException if the driver is not supported
	 */
	@Override
	public void migrateTo(final @NotNull PersistenceDriver driver) throws UnsupportedDriverException {
		boolean epimeralThis = !this.connected();
		boolean epimeralOther = !driver.connected();

		Objects.requireNonNull(driver);

		DataSource fromSource = this.connect().join();
		DataSource toSource = driver.connect().join();

		try {
			try (Connection from = fromSource.getConnection(); Connection to = toSource.getConnection()) {
				DSLContext dslFrom = DSL.using(from, new Settings()
						.withReadonlyInsert(WriteIfReadonly.IGNORE)
						.withReadonlyTableRecordInsert(WriteIfReadonly.IGNORE)
						.withReadonlyUpdatableRecordUpdate(WriteIfReadonly.IGNORE)
						.withReadonlyUpdate(WriteIfReadonly.IGNORE));
				DSLContext dslTo = DSL.using(to);

				var _ = dslTo.createTableIfNotExists(Tables.USERS);
				if (!dslFrom.fetchExists(Tables.USERS)) {
					return;
				}

				int pageSize = 500;
				long lastId = 0;

				while (true) {
					Result<UsersRecord> result = dslFrom.selectFrom(Tables.USERS)
							.where(Tables.USERS.ID.greaterThan(lastId))
							.orderBy(Tables.USERS.ID)
							.limit(pageSize)
							.fetch();

					if (result.isEmpty()) {
						break;
					}

					dslTo.insertInto(Tables.USERS).set(result).executeLarge();
				}
			}
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		} finally {
			if (epimeralThis) {
				tryClose(this);
			}

			if (epimeralOther) {
				tryClose(driver);
			}
		}
	}

	private static void tryClose(final PersistenceDriver driver) {
		try {
			driver.disconnect().join();
		} catch (Throwable _) {}
	}
}
