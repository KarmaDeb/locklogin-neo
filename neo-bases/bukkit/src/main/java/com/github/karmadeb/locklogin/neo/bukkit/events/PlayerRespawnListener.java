package com.github.karmadeb.locklogin.neo.bukkit.events;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession.Stage;
import com.github.karmadeb.locklogin.neo.bukkit.limbo.LimboLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class PlayerRespawnListener implements Listener {

	private final LockLogin<Player> api = LockLogin.get();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(final PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		AuthSession<Player> session = api.persistence().users().session(player).orElseThrow();
		if (!session.stage().equals(Stage.FINISHED)) {
			event.setRespawnLocation(LimboLocation.world().getSpawnLocation());
		}
	}
}
