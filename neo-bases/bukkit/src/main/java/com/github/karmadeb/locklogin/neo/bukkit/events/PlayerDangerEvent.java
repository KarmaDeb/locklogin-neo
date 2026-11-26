package com.github.karmadeb.locklogin.neo.bukkit.events;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession.Stage;
import com.github.karmadeb.locklogin.neo.bukkit.PlatformDecisions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class PlayerDangerEvent implements Listener {

	private final LockLogin<Player> api = LockLogin.get();
	private final PlatformDecisions decisions;

	public PlayerDangerEvent(final PlatformDecisions decisions) {
		this.decisions = decisions;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityTarget(final EntityTargetLivingEntityEvent event) {
		LivingEntity target = event.getTarget();
		if (target instanceof Player player) {
			AuthSession<Player> session = api.persistence().users().session(player).orElseThrow();
			if (!session.stage().equals(Stage.FINISHED)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamage(final EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player player) {
			AuthSession<Player> session = api.persistence().users().session(player).orElseThrow();
			if (!session.stage().equals(Stage.FINISHED)) {
				event.setDamage(0.0d);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDead(final PlayerDeathEvent event) {
		Player player = event.getEntity();
		AuthSession<Player> session = api.persistence().users().session(player).orElseThrow();
		if (!session.stage().equals(Stage.FINISHED)) {
			event.setKeepInventory(true);
			event.setKeepLevel(true);
			event.setDeathMessage("");
			event.setDroppedExp(0);
			event.getDrops().clear();

			// Even though the event is not asynchronous, I've had weird experiences in where
			// not synchronizing causes the player to simply not respawn, even though this was in
			// old bukkit versions, I am not confident enough of it being "fixed" (if it was even
			// a bug in the first place, because maybe the game just needed a little bit to process
			// the player death?)
			Bukkit.getScheduler().runTask(this.decisions.plugin(), () ->
				this.decisions.respawn(player));
		}
	}
}
