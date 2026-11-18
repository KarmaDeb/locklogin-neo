package com.github.karmadeb.locklogin.neo.bukkit.user;

import com.github.karmadeb.locklogin.neo.api.persistence.driver.PersistenceDriver;
import com.github.karmadeb.locklogin.neo.api.user.CacheAccountManager;
import com.github.karmadeb.locklogin.neo.api.user.UserManager;
import com.github.karmadeb.locklogin.neo.api.user.account.Account;
import com.github.karmadeb.locklogin.neo.common.security.authentication.NeoAuthSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the bukkit user manager
 */
public final class BukkitUserManager implements UserManager<Player> {

	/**
	 * Creates a new bukkit user manager
	 * @param driver the underlying driver
	 */
	@Contract("null -> fail")
	public BukkitUserManager(final PersistenceDriver driver) {
	}

	/**
	 * Get the existing session for the specified
	 * player. If for any reason the player does not yet
	 * have a session, this method will return an empty
	 * optional
	 *
	 * @param player the player
	 * @return the player session
	 */
	@Override
	public @NotNull Optional<NeoAuthSession<Player>> session(final @NonNull Player player) {
		return Optional.empty();
	}

	/**
	 * Get the existing account for the specified
	 * player. If the player never connected to the
	 * server or never completed the registration process,
	 * this method will return an empty optional
	 *
	 * @param player the player
	 * @return the player account fetch task
	 */
	@Override
	public @NotNull CompletableFuture<Optional<? extends Account>> account(final @NonNull Player player) {
		return null;
	}

	/**
	 * Get an existing account by its ID. If no record
	 * with ID is present, this method return an empty
	 * optional. The core difference between this and
	 * {@link #account(Player)} is that this method will
	 * return even if the account never completed the
	 * registration process
	 *
	 * @param id the account id
	 * @return the player account fetch task
	 */
	@Override
	public @NotNull CompletableFuture<Optional<? extends Account>> account(final UUID id) {
		return null;
	}

	/**
	 * Get the cache-bound user manager
	 *
	 * @return the manager
	 */
	@Override
	public @NotNull CacheAccountManager<Player> cache() {
		return null;
	}
}
