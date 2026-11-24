package com.github.karmadeb.locklogin.neo.bukkit.events;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession.Stage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class PlayerChatListener implements Listener {

	private final LockLogin<Player> API = LockLogin.get();
	private final Set<String> allowedCommands;

	public PlayerChatListener(final Set<String> commands) {
		this.allowedCommands = commands
				.stream()
				.filter(Objects::nonNull)
				.map(String::toLowerCase).collect(Collectors.toSet());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		AuthSession<Player> session = API.persistence().users().session(player).orElseThrow();
		if (!session.stage().equals(Stage.FINISHED)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerRunCommand(final PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		AuthSession<Player> session = API.persistence().users().session(player).orElseThrow();
		if (!session.stage().equals(Stage.FINISHED)) {
			String command = event.getMessage().trim();
			if (command.isBlank()) {
				event.setCancelled(true);
				return;
			}

			if (command.startsWith("/")) {
				command = command.substring(1);
			}
			String firstPart = command.split("\\s+", 2)[0];
			int colonIndex = firstPart.indexOf(':');
			String commandName = (colonIndex >= 0) ? firstPart.substring(colonIndex + 1) : firstPart;

			if (this.allowedCommands.stream().noneMatch(c -> c.equalsIgnoreCase(commandName))) {
				event.setCancelled(true);
			}
		}
	}
}
