package com.github.karmadeb.locklogin.neo.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The platform decisions is an abstract method
 * of dealing platform specific methods for event
 * handling which should not be moved to platform
 * specific event handling
 */
public abstract class PlatformDecisions {

	/**
	 * Get the current plugin
	 * @return the plugin
	 */
	public abstract JavaPlugin plugin();

	/**
	 * Respawn a player
	 * @param player the player to respawn
	 */
	public abstract void respawn(final Player player);
}
