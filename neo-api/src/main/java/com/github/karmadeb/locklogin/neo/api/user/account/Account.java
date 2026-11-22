package com.github.karmadeb.locklogin.neo.api.user.account;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The account serves as the persistent storage bridge between
 * the database and the plugin. Modifications on this object results
 * in modifications into the database.
 * In order to prevent database with tons of operations, modifying
 * also requires an open transaction to work.
 */
@SuppressWarnings("unused")
public interface Account {

	NamespacedIdentity SECRET_PASSWORD = NamespacedIdentity.locklogin("password");

	/**
	 * Get the stored account UUID. Always represents
	 * an offline-mode UUID
	 * @return the account UUID
	 */
	@NotNull
	UUID uuid();

	/**
	 * Update the account UUID
	 * @param id the new account UUID
	 */
	void uuid(final @NotNull UUID id);

	/**
	 * Get the account username
	 * @return the account owner username
	 */
	@NotNull
	String username();

	/**
	 * Set the account username
	 * @param username the username
	 */
	void username(final @NotNull String username);

	/**
	 * Get a secret
	 * @param id the secret id
	 * @return the secret
	 */
	Optional<String> get(final @NotNull NamespacedIdentity id);

	/**
	 * Put a secret into the account
	 * @param id the secret ID
	 * @param secret the secret value
	 */
	void put(final @NotNull NamespacedIdentity id, final @NotNull String secret);

	/**
	 * Removes a secret from the account
	 * @param id the secret ID
	 */
	void remove(final @NotNull NamespacedIdentity id);

	/**
	 * Get when the account was lastly disconnected
	 * at. As the first time a user joins the server he
	 * has no disconnect records for the plugin, this
	 * method might not always return a valid value.
	 * @return when the account was disconnected from
	 * the server
	 */
	Optional<LocalDateTime> lastDisconnectAt();

	/**
	 * Set when the account was lastly disconnected
	 * @param time the disconnect time
	 */
	void lastDisconnectAt(final @NotNull LocalDateTime time);

	/**
	 * Get when the account was last seen
	 * active
	 * @return the last time the account was
	 * active
	 */
	@NotNull
	LocalDateTime lastSeenAt();

	/**
	 * Set when the account was last seen
	 * active
	 * @param time the last time seen active
	 */
	void lastSeenAt(final @NotNull LocalDateTime time);

	/**
	 * Get when the account was first seen
	 * active. This is not necessarily equal
	 * to when the account was registered
	 * @return when the account was first
	 * seen in the server
	 */
	@NotNull
	LocalDateTime firstSeenAt();

	/**
	 * Set when the account was first seen
	 * active
	 * @param time when the account was first
	 *             seen
	 */
	void firstSeenAt(final @NotNull LocalDateTime time);

	/**
	 * Get when the account issued its
	 * registration request. As an account is not
	 * necessarily registered, this method can still
	 * return an empty optional, indicating the account
	 * is not registered
	 * @return when the account was registered
	 */
	@NotNull
	Optional<LocalDateTime> registeredAt();

	/**
	 * Set when the account issued its
	 * registration request
	 * @param time the registration time
	 */
	void registeredAt(final @NotNull LocalDateTime time);

	/**
	 * Determines if the account owner is present.
	 * Do not take the return value of this method
	 * as a source of truth, as it only checks the
	 * last seen and last disconnect, returning true
	 * only if last seen is closer to now than last
	 * disconnect
	 * @return if the account owner is potentially present
	 */
	default boolean isPresent() {
		Optional<LocalDateTime> disconnectAt = this.lastDisconnectAt();
		if (disconnectAt.isEmpty()) {
			return true;
		}

		LocalDateTime seen = this.lastSeenAt();
		LocalDateTime disconnected = disconnectAt.get();

		return seen.isAfter(disconnected);
	}

	/**
	 * Save the account in the persistence
	 * storage
	 * @return the save task
	 */
	@NotNull
	CompletableFuture<Void> save();
}
