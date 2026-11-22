package com.github.karmadeb.locklogin.neo.common.auth.platform.bukkit.layer;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession;
import com.github.karmadeb.locklogin.neo.common.auth.AuthMetadataValue;
import com.github.karmadeb.locklogin.neo.common.auth.config.RegistrationConfig;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PasswordRegisterLayer implements AuthLayer<Player> {

	public static final NamespacedIdentity ID = NamespacedIdentity.locklogin("password-register");
	private final JavaPlugin plugin;
	private final RegistrationConfig config;

	public PasswordRegisterLayer(final JavaPlugin plugin, final RegistrationConfig config) {
		this.plugin = plugin;
		this.config = config;
	}

	/**
	 * Get the layer id
	 *
	 * @return the ID of the layer
	 */
	@Override
	public @NotNull NamespacedIdentity id() {
		return ID;
	}

	/**
	 * Get the layer type. This is essentially required
	 * for in-runtime validation of player object type
	 * before calling to {@link #challenge(Player)}. It's important
	 * to specify the object type correctly, for instance
	 * {@code org.bukkit.entity.Player.class} for Bukkit.
	 * Alternatively, you can use the experimental "neo-api-bridge"
	 * API to have a "centralized" way of interacting with players
	 * and the server object, even though it is not currently recommended
	 *
	 * @return the layer type
	 */
	@Override
	public @NotNull Class<Player> type() {
		return Player.class;
	}

	/**
	 * Determines if this layer is registered
	 * automatically
	 *
	 * @return if the layer is registered
	 */
	@Override
	public boolean autoRegister() {
		return true;
	}

	/**
	 * Determines if this layer is required. When false
	 * it means that regardless of {@link Result#KICK kick}
	 * failure, the user receives a message in chat in case
	 * of fail, unless reason is {@link Result#SILENT silent}
	 * or success
	 *
	 * @return if the layer is required
	 */
	@Override
	public boolean required() {
		return true;
	}

	/**
	 * Get the timeout for the player to complete
	 * the challenge. If not provided, no timeout
	 * is enabled
	 *
	 * @return the layer authentication timeout
	 */
	@Override
	public @NotNull Optional<Integer> timeout() {
		return this.config.registerTimeout();
	}

	/**
	 * Challenge the player. When the method returns true, the
	 * authentication passes, otherwise it fails. It's important to not
	 * misbehave this method in returning true instantly if the player
	 * does not apply for the challenge (i.e: an auth layer which is for
	 * staff members only). Instead, the {@link AuthSession#disable(NamespacedIdentity)}
	 * should be used in order to prevent dirtying the auditor logs.
	 *
	 * @param player the player to challenge
	 * @return the challenge result
	 */
	@Override
	public @NotNull Hook challenge(final @NotNull Player player) {
		Hook hook = new Hook();

		player.setMetadata("neo-challenge", new AuthMetadataValue(hook, this.plugin));
		return hook;
	}
}
