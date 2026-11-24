package com.github.karmadeb.locklogin.neo.bukkit;

import com.github.karmadeb.locklogin.neo.api.essentials.EssentialFeatures;
import com.github.karmadeb.locklogin.neo.common.auth.AuthEntrypoint;
import com.github.karmadeb.locklogin.neo.common.crypto.CryptoEntrypoint;
import com.github.karmadeb.locklogin.neo.common.jooq.JooQEntrypoint;
import org.bukkit.plugin.java.JavaPlugin;

public final class EssentialsLoader {

	private final CryptoEntrypoint crypto = new CryptoEntrypoint();
	private final AuthEntrypoint auth = new AuthEntrypoint();
	private final JooQEntrypoint jooq = new JooQEntrypoint();

	private final JavaPlugin plugin;

	public EssentialsLoader(final JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void configure() {
		crypto.setupConfiguration();
		auth.setupConfiguration();
		jooq.setupConfiguration();
	}

	public void setup() {
		if (EssentialFeatures.has(EssentialFeatures.FEATURE_CRYPTO)) {
			plugin.getLogger().info("Setting up locklogin essentials crypto");
			crypto.setup();
		}
		if (EssentialFeatures.has(EssentialFeatures.FEATURE_AUTH)) {
			plugin.getLogger().info("Setting up locklogin essentials auth");
			auth.setup();
		}
		if (EssentialFeatures.has(EssentialFeatures.FEATURE_JOOQ)) {
			plugin.getLogger().info("Setting up locklogin essentials jooq");
			jooq.setup();
		}
	}

	public void postSetup() {
		if (EssentialFeatures.has(EssentialFeatures.FEATURE_AUTH)) {
			plugin.getLogger().info("Applying post-setup of locklogin essentials crypto");
			auth.bootstrap().bukkit().init(this.plugin);
		}
	}
}
