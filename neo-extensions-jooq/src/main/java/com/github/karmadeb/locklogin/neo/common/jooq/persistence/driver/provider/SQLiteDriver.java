package com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider;

import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.JooQDriver;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jooq.SQLDialect;
import org.sqlite.JDBC;

public final class SQLiteDriver extends JooQDriver {

	public SQLiteDriver() {
		super(JDBC.class, SQLDialect.SQLITE);
	}

	@Override
	protected @NotNull HikariDataSource configure() {
		return null;
	}

}
