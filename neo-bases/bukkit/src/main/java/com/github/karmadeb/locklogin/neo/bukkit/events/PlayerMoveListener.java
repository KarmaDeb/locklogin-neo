package com.github.karmadeb.locklogin.neo.bukkit.events;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession.Stage;
import com.github.karmadeb.locklogin.neo.bukkit.limbo.LimboLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class PlayerMoveListener implements Listener {

	private final LockLogin<Player> api = LockLogin.get();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onAnimation(final PlayerAnimationEvent event) {
		Player player = event.getPlayer();
		AuthSession<Player> session = api.persistence().users().session(player).orElseThrow();
		if (!session.stage().equals(Stage.FINISHED)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInteraction(final PlayerInteractEvent event) {
		Player player = event.getPlayer();
		AuthSession<Player> session = api.persistence().users().session(player).orElseThrow();
		if (!session.stage().equals(Stage.FINISHED)) {
			event.setCancelled(true);
			event.setUseInteractedBlock(Result.DENY);
			event.setUseItemInHand(Result.DENY);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent event) {
		Player player = event.getPlayer();
		AuthSession<Player> session = api.persistence().users().session(player).orElseThrow();
		if (!session.stage().equals(Stage.FINISHED)) {
			Location from = event.getFrom();
			Location to = event.getTo();
			if (to == null) {
				return;
			}

			int fromX = from.getBlockX();
			int fromY = from.getBlockY();
			int fromZ = from.getBlockZ();

			int toX = to.getBlockX();
			int toY = to.getBlockY();
			int toZ = to.getBlockZ();

			if (fromX != toX || fromZ != toZ || fromY < toY) {
				event.setTo(event.getFrom());
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		AuthSession<Player> session = api.persistence().users().session(player).orElseThrow();
		if (!session.stage().equals(Stage.FINISHED)) {
			Location destination = event.getTo();
			if (destination == null) {
				return;
			}

			World target = destination.getWorld();
			if (target == null) {
				return;
			}

			if (!target.getUID().equals(LimboLocation.world().getUID())) {
				event.setCancelled(true);
			}
		}
	}
}
