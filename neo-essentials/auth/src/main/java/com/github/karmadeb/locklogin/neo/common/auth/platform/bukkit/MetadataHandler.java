package com.github.karmadeb.locklogin.neo.common.auth.platform.bukkit;

import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer.Hook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

import static com.github.karmadeb.locklogin.neo.common.auth.AuthEntrypoint.CHALLENGE_METADATA_KEY;

public final class MetadataHandler {

	private final JavaPlugin plugin;
	private final Player player;

	public MetadataHandler(final JavaPlugin plugin, final Player player) {
		this.plugin = plugin;
		this.player = player;
	}

	public Optional<Hook> hook() {
		return player.getMetadata(CHALLENGE_METADATA_KEY).stream()
				.filter(v -> v instanceof AuthMetadataValue m && m.value() != null)
				.map(AuthMetadataValue.class::cast)
				.findAny()
				.map(AuthMetadataValue::value);
	}

	public void hook(final Hook hook) {
		player.setMetadata(CHALLENGE_METADATA_KEY, new AuthMetadataValue(hook, this.plugin));
	}
}
