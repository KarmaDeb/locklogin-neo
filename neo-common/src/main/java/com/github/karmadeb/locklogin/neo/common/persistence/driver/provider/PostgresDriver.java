package com.github.karmadeb.locklogin.neo.common.persistence.driver.provider;

import com.github.karmadeb.locklogin.neo.api.persistence.driver.PersistenceDriver;
import com.github.karmadeb.locklogin.neo.api.persistence.exception.UnsupportedDriverException;
import com.github.karmadeb.locklogin.neo.common.persistence.driver.GenericDriver;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.postgresql.Driver;

public final class PostgresDriver extends GenericDriver {

	public PostgresDriver() {
		super(Driver.class, "postgres");
	}

	@Override
	protected @NotNull HikariDataSource configure() {
		return null;
	}

	/**
	 * Migrate to a different driver from the
	 * current one. Custom driver implementations should
	 * always add support for this -> default SQL drivers
	 *
	 * @param driver the driver to migrate to
	 * @throws UnsupportedDriverException if the driver is not supported
	 */
	@Override
	public void migrateTo(final @NotNull PersistenceDriver<?, ?> driver) throws UnsupportedDriverException {

	}
}
