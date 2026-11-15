package com.github.karmadeb.locklogin.neo.common.persistence.driver;

import com.github.karmadeb.locklogin.neo.api.Constants;
import com.github.karmadeb.locklogin.neo.api.SneakyFunction;
import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.persistence.driver.ResultSetDriver;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public abstract class GenericDriver implements ResultSetDriver {

	protected static final ExecutorService SQL_ASYNC_EXECUTOR = Executors.newWorkStealingPool();

	protected final Class<?> driver;
	protected final NamespacedIdentity id;
	private final AtomicReference<CompletableFuture<DataSource>> connectTaskRef = new AtomicReference<>();
	private final AtomicReference<CompletableFuture<Void>> closeTaskRef = new AtomicReference<>();

	protected HikariDataSource dataSource;

	@Contract("null, _ -> fail; _, null -> fail")
	public GenericDriver(final Class<?> driver, final String resource) {
		this.driver = driver;
		this.id = NamespacedIdentity.create(Constants.LOCKLOGIN_EXTENSION_ID, resource);
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
	public final @NotNull CompletableFuture<DataSource> connect() {
		if (this.dataSource != null && !this.dataSource.isClosed()) {
			return CompletableFuture.completedFuture(this.dataSource);
		}

		CompletableFuture<DataSource> existing = connectTaskRef.get();
		if (existing != null && !existing.isCompletedExceptionally() && !existing.isCancelled()) {
			return existing;
		}

		CompletableFuture<DataSource> newTask = new CompletableFuture<>();
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

	/**
	 * Execute a query
	 *
	 * @param function the function to execute
	 * @return the query result task
	 */
	@Override
	public @NotNull CompletableFuture<ResultSet> query(final @NotNull SneakyFunction<Connection, ResultSet> function) {
		if (!this.connected()) {
			return CompletableFuture.failedFuture(new SQLException("DataSource not connected"));
		}

		CompletableFuture<ResultSet> task = new CompletableFuture<>();

		SQL_ASYNC_EXECUTOR.submit(() -> {
			try(Connection connection = this.dataSource.getConnection()) {
				ResultSet rs = function.apply(connection);
				task.complete(rs);
			} catch (Throwable ex) {
				task.completeExceptionally(ex);
			}
		});

		return task;
	}

	@NotNull
	protected abstract HikariDataSource configure();
}
