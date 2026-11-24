package com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider;

import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigFile;
import com.github.karmadeb.locklogin.neo.common.jooq.config.SQLConfiguration;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.JooQDriver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jooq.SQLDialect;
import org.sqlite.JDBC;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.JournalMode;
import org.sqlite.SQLiteConfig.SynchronousMode;
import org.sqlite.SQLiteConfig.TempStore;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.Objects;

public final class SQLiteDriver extends JooQDriver {

	private final SQLConfiguration config;

	@Contract("null -> fail")
	public SQLiteDriver(final SQLConfiguration config) {
		super(JDBC.class, SQLDialect.SQLITE);
		this.config = Objects.requireNonNull(config);
	}

	@Override
	protected @NotNull DataSource configure() {
		String database = config.database();
		String fileName = database.endsWith(".db") ? database : database + ".db";
		Path dbFile = ConfigFile.PLUGIN_CONFIG.path().getParent().resolve(fileName);

		SQLiteConfig sqliteConfig = new SQLiteConfig();
		sqliteConfig.setJournalMode(JournalMode.WAL);
		sqliteConfig.setSynchronous(SynchronousMode.NORMAL);
		sqliteConfig.setBusyTimeout((int) Math.min(config.connectionTimeout(), Integer.MAX_VALUE));
		sqliteConfig.enforceForeignKeys(true);
		sqliteConfig.setTempStore(TempStore.MEMORY);

		SQLiteDataSource dataSource = new SQLiteDataSource(sqliteConfig);
		dataSource.setUrl("jdbc:sqlite:" + dbFile.toAbsolutePath());

		return dataSource;
	}
}
