package com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider;

import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.JooQDriver;
import com.mysql.cj.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jooq.SQLDialect;

public final class MySQLDriver extends JooQDriver {

	public MySQLDriver() {
		super(Driver.class, SQLDialect.MYSQL);
	}

	@Override
	protected @NotNull HikariDataSource configure() {
		return null;
	}
}
