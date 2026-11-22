package com.github.karmadeb.locklogin.neo.api.agnostic;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import org.jetbrains.annotations.NotNull;

/**
 * The server platforms helps extensions to identify
 * which platform the plugin is currently running on.
 * This is useless for extensions intended to run on a
 * single specific platform, but for multi-platform support
 * this is an essential part of the API which must be handled
 * gracefully
 */
public interface ServerPlatform {

	/**
	 * Get the platform id, unlike many parts of the
	 * API, the namespaced identify of the platform is
	 * composed of two parts; the base platform API, and
	 * the specific vendor. For instance, a PaperMC server
	 * is {@code bukkit:paper}, a SpigotMC server is {@code bukkit:spigot}
	 * or a BungeeCord proxy {@code proxy:bungee}, Velocity {@code proxy:velocity}.
	 * @return the platform id
	 */
	@NotNull
	NamespacedIdentity id();

	/**
	 * Get the platform provider. The provider is not the
	 * platform itself, but which plugin provides this platform.
	 * For native-supported this is always an instance of {@link com.github.karmadeb.locklogin.neo.api.LockLogin}
	 * @return the platform provider
	 */
	@NotNull
	Class<?> provider();
}
