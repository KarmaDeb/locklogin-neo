package com.github.karmadeb.locklogin.neo.bukkit.events;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession.Stage;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public final class PlayerMoveListener implements Listener {

	private final LockLogin<Player> API = LockLogin.get();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onAnimation(final PlayerAnimationEvent event) {
		Player player = event.getPlayer();
		AuthSession<Player> session = API.persistence().users().session(player).orElseThrow();
		if (!session.stage().equals(Stage.FINISHED)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInteraction(final PlayerInteractEvent event) {
		Player player = event.getPlayer();
		AuthSession<Player> session = API.persistence().users().session(player).orElseThrow();
		if (!session.stage().equals(Stage.FINISHED)) {
			event.setCancelled(true);
			event.setUseInteractedBlock(Result.DENY);
			event.setUseItemInHand(Result.DENY);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent event) {
		Player player = event.getPlayer();
		AuthSession<Player> session = API.persistence().users().session(player).orElseThrow();
		if (!session.stage().equals(Stage.FINISHED)) {
			event.setTo(event.getFrom());
			event.setCancelled(true);
		}
	}
}
