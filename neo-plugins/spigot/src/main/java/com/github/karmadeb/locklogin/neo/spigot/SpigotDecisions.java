package com.github.karmadeb.locklogin.neo.spigot;

import com.github.karmadeb.locklogin.neo.bukkit.PlatformDecisions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpigotDecisions extends PlatformDecisions {

	private final JavaPlugin plugin;

	public SpigotDecisions(final JavaPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Get the current plugin
	 *
	 * @return the plugin
	 */
	@Override
	public JavaPlugin plugin() {
		return this.plugin;
	}

	/**
	 * Respawn a player
	 *
	 * @param player the player to respawn
	 */
	@Override
	public void respawn(final Player player) {
    	if (!player.isOnline() || !player.isDead()) {
			return;
		}

		player.spigot().respawn();
	}
}
