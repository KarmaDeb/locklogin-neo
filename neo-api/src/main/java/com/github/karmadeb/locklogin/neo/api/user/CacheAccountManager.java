package com.github.karmadeb.locklogin.neo.api.user;

import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession;
import com.github.karmadeb.locklogin.neo.api.user.account.Account;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * The cache account manager behaves exactly as
 * the {@link UserManager} with the core difference
 * it uses cached data only. This is useful for records
 * which are being accessed frequently, and you know are
 * already in-cache. The existence of this class does not
 * imply that user manager never uses cache, it's just a
 * helper class which also helps to reduce object boilerplate
 * when dealing with cached entries
 * @param <T> the player type
 */
public interface CacheAccountManager<T> {

	/**
	 * Get back the db-bound manager
	 * @return the user manager
	 */
	@NotNull
	UserManager<T> manager();

	/**
	 * Get the existing session for the specified
	 * player. If for any reason the player does not yet
	 * have a session, this method will return an empty
	 * optional
	 * @param player the player
	 * @return the player session
	 */
	@NotNull
	default Optional<? extends AuthSession<T>> session(final @NotNull T player) {
		return this.manager().session(player);
	}

	/**
	 * Get the existing account for the specified
	 * player. If the player never connected to the
	 * server or never completed the registration process,
	 * this method will return an empty optional
	 * @param player the player
	 * @return the player account fetch task
	 */
	@NotNull
	Optional<? extends Account> account(final @NotNull T player);

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
	Optional<? extends Account> account(final UUID id);
}
