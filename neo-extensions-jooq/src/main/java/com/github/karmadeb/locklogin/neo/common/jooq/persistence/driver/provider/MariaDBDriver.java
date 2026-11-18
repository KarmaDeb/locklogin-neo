package com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider;

import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.JooQDriver;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jooq.SQLDialect;
import org.mariadb.jdbc.Driver;

public final class MariaDBDriver extends JooQDriver {

	public MariaDBDriver() {
		super(Driver.class, SQLDialect.MARIADB);
	}

	@Override
	protected @NotNull HikariDataSource configure() {
		return null;
	}
}
