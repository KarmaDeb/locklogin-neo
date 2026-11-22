package com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider;

import com.github.karmadeb.locklogin.neo.common.jooq.config.SQLConfiguration;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.JooQDriver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jooq.SQLDialect;
import org.postgresql.Driver;

import java.util.Objects;

public final class PostgresDriver extends JooQDriver {

	private final SQLConfiguration config;

	@Contract("null -> fail")
	public PostgresDriver(final SQLConfiguration config) {
		super(Driver.class, SQLDialect.POSTGRES);
		this.config = Objects.requireNonNull(config);
	}

	@Override
	protected @NotNull HikariDataSource configure() {
		HikariConfig settings = new HikariConfig();
		settings.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", this.config.host(),
				this.config.port(),
				this.config.database()));

		settings.setUsername(this.config.username());
		settings.setPassword(this.config.password());
		settings.setMaximumPoolSize(this.config.maxPoolSize());
		settings.setMinimumIdle(this.config.minPoolSize());
		settings.setConnectionTimeout(this.config.connectionTimeout());
		settings.setIdleTimeout(this.config.idleTimeout());
		settings.setMaxLifetime(this.config.lifetime());
		settings.setValidationTimeout(this.config.validationTimeout());

		long keepAlive = config.keepAlive();
		if (keepAlive > 0) {
			settings.setKeepaliveTime(keepAlive);
		}

		settings.setPoolName("locklogin-jooq-pgsql");
		return new HikariDataSource(settings);
	}
}
