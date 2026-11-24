package com.github.karmadeb.locklogin.neo.common.crypto;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigFile;
import com.github.karmadeb.locklogin.neo.api.security.HashManager.Purpose;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.argon.Argon2HashMethod;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.bcrypt.BCryptHashMethod;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.scrypt.SCryptHashMethod;
import com.github.karmadeb.locklogin.neo.common.crypto.config.argon.Argon2Configuration;
import com.github.karmadeb.locklogin.neo.common.crypto.config.bcrypt.BCryptConfiguration;
import com.github.karmadeb.locklogin.neo.common.crypto.config.scrypt.SCryptConfiguration;

@SuppressWarnings("unused")
public final class CryptoEntrypoint {

	public static final ConfigFile ALGORITHMS_CONFIG = new ConfigFile("crypto");

	/**
	 * Logic to run after an extension has been loaded. This does not
	 * necessarily mean the extension is currently enabled, but just that
	 * LockLogin recognized it as an extension
	 */
	public void setupConfiguration() {
		LockLogin<?> api = LockLogin.get();

		api.configurations().append(ALGORITHMS_CONFIG, new Argon2Configuration());
		api.configurations().append(ALGORITHMS_CONFIG, new BCryptConfiguration());
		api.configurations().append(ALGORITHMS_CONFIG, new SCryptConfiguration());
	}

	/**
	 * Logic to run after an extension has been enabled
	 */
	public void setup() {
		LockLogin<?> api = LockLogin.get();

		Argon2Configuration argon2 = api.configurations().part(ALGORITHMS_CONFIG, Argon2Configuration.class)
				.orElseThrow();
		BCryptConfiguration bcrypt = api.configurations().part(ALGORITHMS_CONFIG, BCryptConfiguration.class)
				.orElseThrow();
		SCryptConfiguration scrypt = api.configurations().part(ALGORITHMS_CONFIG, SCryptConfiguration.class)
				.orElseThrow();

    	api.hashes().register(new Argon2HashMethod(argon2), Purpose.PASSWORD);
    	api.hashes().register(new BCryptHashMethod(bcrypt), Purpose.PASSWORD);
    	api.hashes().register(new SCryptHashMethod(scrypt), Purpose.PASSWORD);
	}
}
