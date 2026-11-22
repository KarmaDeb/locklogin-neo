package com.github.karmadeb.locklogin.neo.common.crypto;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigFile;
import com.github.karmadeb.locklogin.neo.api.extension.ExtensionLifecycle;
import com.github.karmadeb.locklogin.neo.api.extension.NeoExtension;
import com.github.karmadeb.locklogin.neo.api.extension.description.annotation.AuthorMeta;
import com.github.karmadeb.locklogin.neo.api.extension.description.annotation.ExtensionMeta;
import com.github.karmadeb.locklogin.neo.api.extension.description.annotation.VersionMeta;
import com.github.karmadeb.locklogin.neo.api.extension.registry.ExtensionRegistryType.Conflict;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.argon.Argon2HashMethod;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.bcrypt.BCryptHashMethod;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.scrypt.SCryptHashMethod;
import com.github.karmadeb.locklogin.neo.common.crypto.config.argon.Argon2Configuration;
import com.github.karmadeb.locklogin.neo.common.crypto.config.bcrypt.BCryptConfiguration;
import com.github.karmadeb.locklogin.neo.common.crypto.config.scrypt.SCryptConfiguration;
import org.jetbrains.annotations.NotNull;


@ExtensionMeta(
		name = "LockLogin crypto",
		id = "neo-essentials-crypto",
		description = """
		Provides official LockLogin support out of the box for
		different crypto algorithms such as argon2 and bcrypt
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

	public static final ConfigFile ALGORITHMS_CONFIG = new ConfigFile("crypto");

	/**
	 * Logic to run after an extension has been loaded. This does not
	 * necessarily mean the extension is currently enabled, but just that
	 * LockLogin recognized it as an extension
	 *
	 * @param lifecycle the lifecycle involved in the operation
	 */
	@Override
	public void loaded(final @NotNull ExtensionLifecycle lifecycle) {
		LockLogin<?> api = LockLogin.get();

		api.configurations().append(ALGORITHMS_CONFIG, new Argon2Configuration());
		api.configurations().append(ALGORITHMS_CONFIG, new BCryptConfiguration());
		api.configurations().append(ALGORITHMS_CONFIG, new SCryptConfiguration());
	}

	/**
	 * Logic to run after an extension has been enabled
	 *
	 * @param lifecycle the lifecycle involved in the operation
	 */
	@Override
	public void enabled(final @NotNull ExtensionLifecycle lifecycle) {
		LockLogin<?> api = LockLogin.get();

		Argon2Configuration argon2 = api.configurations().part(ALGORITHMS_CONFIG, Argon2Configuration.class)
				.orElseThrow();
		BCryptConfiguration bcrypt = api.configurations().part(ALGORITHMS_CONFIG, BCryptConfiguration.class)
				.orElseThrow();
		SCryptConfiguration scrypt = api.configurations().part(ALGORITHMS_CONFIG, SCryptConfiguration.class)
				.orElseThrow();

		try {
			this.registry().hash().register(new Argon2HashMethod(argon2));
			this.registry().hash().register(new BCryptHashMethod(bcrypt));
			this.registry().hash().register(new SCryptHashMethod(scrypt));
		} catch (Conflict _) {}
	}
}
