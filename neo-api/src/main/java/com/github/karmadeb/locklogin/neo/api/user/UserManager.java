package com.github.karmadeb.locklogin.neo.api.user;

import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession;
import com.github.karmadeb.locklogin.neo.api.user.account.Account;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The user manager API allows to load accounts
 * and sessions of a player.
 * @param <T> the player type
 */
public interface UserManager<T> {

	/**
	 * Get the existing session for the specified
	 * player. If for any reason the player does not yet
	 * have a session, this method will return an empty
	 * optional
	 * @param player the player
	 * @return the player session
	 */
	@NotNull
	Optional<? extends AuthSession<T>> session(final @NotNull T player);

	/**
	 * Get the existing account for the specified
	 * player. If the player never connected to the
	 * server or never completed the registration process,
	 * this method will return an empty optional
	 * @param player the player
	 * @return the player account fetch task
	 */
	@NotNull
	CompletableFuture<Optional<? extends Account>> account(final @NotNull T player);

	/**
	 * Get an existing account by its ID. If no record
	 * with ID is present, this method return an empty
	 * optional. The core difference between this and
	 * {@link #account(Object)} is that this method will
	 * return even if the account never completed the
	 * registration process
	 * @param id the account id
	 * @return the player account fetch task
	 */
	@NotNull
	CompletableFuture<Optional<? extends Account>> account(final UUID id);

	/**
	 * Get the cache-bound user manager
	 * @return the manager
	 */
	@NotNull
	CacheAccountManager<T> cache();
}