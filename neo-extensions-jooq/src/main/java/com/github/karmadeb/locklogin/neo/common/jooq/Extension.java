package com.github.karmadeb.locklogin.neo.common.jooq;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigFile;
import com.github.karmadeb.locklogin.neo.api.extension.ExtensionLifecycle;
import com.github.karmadeb.locklogin.neo.api.extension.NeoExtension;
import com.github.karmadeb.locklogin.neo.api.extension.description.annotation.AuthorMeta;
import com.github.karmadeb.locklogin.neo.api.extension.description.annotation.ExtensionMeta;
import com.github.karmadeb.locklogin.neo.api.extension.description.annotation.VersionMeta;
import com.github.karmadeb.locklogin.neo.api.persistence.PersistenceManager;
import com.github.karmadeb.locklogin.neo.common.jooq.config.SQLConfiguration;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.JooQBukkitPersistence;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.JooQDriver;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider.MariaDBDriver;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider.MySQLDriver;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider.PostgresDriver;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.provider.SQLiteDriver;
import org.jetbrains.annotations.NotNull;

@ExtensionMeta(
		name = "LockLogin JOOQ",
		id = "neo-essentials-jooq",
		description = """
		Provides official LockLogin support out of the box for
		multiple SQL dialects through the use of JOOQ library
		""",
		version = @VersionMeta(major = 1, minor = 0, patch = 0),
		authors = {
				@AuthorMeta(
						username = "KarmaDev",
						address = "karmaconfigs@gmail.com"
				)
		}
)
public final class Extension extends NeoExtension {

	/**
	 * Logic to run after an extension has been loaded. This does not
	 * necessarily mean the extension is currently enabled, but just that
	 * LockLogin recognized it as an extension
	 *
	 * @param lifecycle the lifecycle involved in the operation
	 */
	@Override
	public void loaded(final @NotNull ExtensionLifecycle lifecycle) {
		if (!lifecycle.equals(ExtensionLifecycle.PLUGIN_LOAD)) {
			throw new UnsupportedOperationException("Cannot load neo-essentials-jooq. Extension was loaded by command");
		}

		LockLogin<?> api = LockLogin.get();
		api.configurations().append(ConfigFile.PLUGIN_CONFIG, new SQLConfiguration());
	}

	/**
	 * Logic to run after an extension has been enabled
	 *
	 * @param lifecycle the lifecycle involved in the operation
	 */
	@Override
	public void enabled(final @NotNull ExtensionLifecycle lifecycle) {
		LockLogin<?> api = LockLogin.get();
		SQLConfiguration configuration = api.configurations().part(ConfigFile.PLUGIN_CONFIG, SQLConfiguration.class)
				.orElseThrow();

		JooQDriver driver = switch (configuration.dialect()) {
			case SQLITE -> new SQLiteDriver();
			case POSTGRES -> new PostgresDriver();
			case MYSQL -> new MySQLDriver();
			case MARIADB -> new MariaDBDriver();
			case null, default -> throw new UnsupportedOperationException("Driver not supported: " + configuration.dialect());
		};

		// TODO: Detect or let the platform decide?
		PersistenceManager manager = new JooQBukkitPersistence(driver);
		api.persistence(manager);
	}
}