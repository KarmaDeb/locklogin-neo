package com.github.karmadeb.locklogin.neo.common.auth;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.api.configuration.registry.ConfigFile;
import com.github.karmadeb.locklogin.neo.common.auth.config.RegistrationConfig;
import com.github.karmadeb.locklogin.neo.common.auth.platform.PlatformBootstrap;

@SuppressWarnings("unused")
public final class AuthEntrypoint {

	public static final String CHALLENGE_METADATA_KEY = "neo-challenge";
	private PlatformBootstrap bootstrap;

	/**
	 * Logic to run after an extension has been loaded. This does not
	 * necessarily mean the extension is currently enabled, but just that
	 * LockLogin recognized it as an extension
	 */
	public void setupConfiguration() {
		LockLogin<?> api = LockLogin.get();

		api.configurations().append(ConfigFile.PLUGIN_CONFIG, new RegistrationConfig());
	}

	/**
	 * Logic to run after an extension has been enabled
	 */
	public void setup() {
		LockLogin<?> api = LockLogin.get();

		RegistrationConfig config = api.configurations().part(ConfigFile.PLUGIN_CONFIG, RegistrationConfig.class)
				.orElseThrow();
		this.bootstrap = new PlatformBootstrap(config);
	}

	public PlatformBootstrap bootstrap() {
		return bootstrap;
	}
}
