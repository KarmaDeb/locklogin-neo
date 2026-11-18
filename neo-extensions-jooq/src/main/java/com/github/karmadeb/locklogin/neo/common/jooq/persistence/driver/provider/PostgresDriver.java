package com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider;

import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.JooQDriver;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jooq.SQLDialect;
import org.postgresql.Driver;

public final class PostgresDriver extends JooQDriver {

	public PostgresDriver() {
		super(Driver.class, SQLDialect.POSTGRES);
	}

	@Override
	protected @NotNull HikariDataSource configure() {
		return null;
	}
}
