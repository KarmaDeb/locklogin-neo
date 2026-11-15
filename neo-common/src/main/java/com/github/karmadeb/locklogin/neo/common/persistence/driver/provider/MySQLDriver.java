package com.github.karmadeb.locklogin.neo.common.persistence.driver.provider;

import com.github.karmadeb.locklogin.neo.api.persistence.driver.PersistenceDriver;
import com.github.karmadeb.locklogin.neo.api.persistence.exception.UnsupportedDriverException;
import com.github.karmadeb.locklogin.neo.common.persistence.driver.GenericDriver;
import com.mysql.cj.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

public final class MySQLDriver extends GenericDriver {

	public MySQLDriver() {
		super(Driver.class, "mysql");
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
