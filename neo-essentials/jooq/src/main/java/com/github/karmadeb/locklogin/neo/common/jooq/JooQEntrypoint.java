package com.github.karmadeb.locklogin.neo.common.jooq;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigFile;
import com.github.karmadeb.locklogin.neo.api.persistence.PersistenceManager;
import com.github.karmadeb.locklogin.neo.common.jooq.config.SQLConfiguration;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.JooQBukkitPersistence;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.JooQDriver;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider.MariaDBDriver;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider.MySQLDriver;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider.PostgresDriver;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider.SQLiteDriver;
import org.jooq.SQLDialect;

import java.util.NoSuchElementException;

@SuppressWarnings("unused")
public final class JooQEntrypoint {

	public void setupConfiguration() {
		LockLogin<?> api = LockLogin.get();
		api.configurations().append(ConfigFile.PLUGIN_CONFIG, new SQLConfiguration());
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void setup() {
		LockLogin<?> api = LockLogin.get();
		SQLConfiguration configuration = api.configurations().part(ConfigFile.PLUGIN_CONFIG, SQLConfiguration.class)
				.orElseThrow();

		JooQDriver.register(SQLDialect.SQLITE, new SQLiteDriver(configuration));
		JooQDriver.register(SQLDialect.POSTGRES, new PostgresDriver(configuration));
		JooQDriver.register(SQLDialect.MYSQL, new MySQLDriver(configuration));
		JooQDriver.register(SQLDialect.MARIADB, new MariaDBDriver(configuration));

		JooQDriver driver = JooQDriver.resolve(configuration.dialect())
				.orElseThrow(() -> new NoSuchElementException("Driver not supported: " + configuration.dialect()));

		NamespacedIdentity id = api.platform().id();
		PersistenceManager manager = switch (id.resource()) {
			case "bukkit" -> new JooQBukkitPersistence(driver);
			case "proxy" -> {
				switch (id.resource()) {
					case "bungee", "velocity" -> throw new UnsupportedOperationException("Not implemented");
					default -> throw new IllegalStateException("Unsupported JooQ extension platform: " + id.resource());
				}
			}
			default -> throw new IllegalStateException("Unsupported JooQ extension platform: " + id.resource());
		};

		api.persistence(manager);
	}
}