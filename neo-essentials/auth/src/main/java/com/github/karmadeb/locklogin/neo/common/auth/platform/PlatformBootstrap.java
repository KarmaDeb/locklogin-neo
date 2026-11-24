package com.github.karmadeb.locklogin.neo.common.auth.platform;

import com.github.karmadeb.locklogin.neo.common.auth.config.RegistrationConfig;
import com.github.karmadeb.locklogin.neo.common.auth.platform.bukkit.BukkitPlatform;

public record PlatformBootstrap(BukkitPlatform bukkit) {

	public PlatformBootstrap(final RegistrationConfig config) {
		this(new BukkitPlatform(config));
	}
}
