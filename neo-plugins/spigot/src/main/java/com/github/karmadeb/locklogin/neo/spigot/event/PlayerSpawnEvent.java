package com.github.karmadeb.locklogin.neo.spigot.event;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.bukkit.limbo.LimboLocation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public final class PlayerSpawnEvent implements Listener {

	private final LockLogin<Player> api = LockLogin.get();
	private final JavaPlugin plugin;

	public PlayerSpawnEvent(final JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSpawn(final PlayerSpawnLocationEvent event) {
		Player player = event.getPlayer();
		Location targetLocation = event.getSpawnLocation();

		LimboLocation location = new LimboLocation(plugin, player);
		event.setSpawnLocation(location.limbo());
		location.save(targetLocation);

		api.persistence().users().session(player).ifPresent(session ->
				session.then((rs, fail) -> {
					if (rs) {
						location.restore();
						return;
					}

					switch (fail.result()) {
						case KICK -> player.kickPlayer(ChatColor.translateAlternateColorCodes('&', fail.reason()));
						case MESSAGE -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', fail.reason()));
					}
				}));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onJoin(final PlayerJoinEvent event) {
		Player player = event.getPlayer();
		api.persistence().users().session(player).ifPresent(session ->
				session.start(player));
	}
}
