package com.github.karmadeb.locklogin.neo.common.jooq.persistence.user;

import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.user.account.Account;
import com.github.karmadeb.locklogin.neo.common.jooq.orm.tables.records.UsersRecord;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.JooQDriver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jooq.JSON;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Scheduled for removal as this will be moved along the user manager
 * to the jooq extension
 */
public final class NeoUserAccount implements Account {

	private static final Gson gson = new GsonBuilder().create();

	private final JooQDriver driver;
	private final UsersRecord user;
	private final Map<NamespacedIdentity, String> mappedSecrets = new ConcurrentHashMap<>();

	@Contract("null, _ -> fail; _, null -> fail")
	public NeoUserAccount(final JooQDriver driver, final UsersRecord user) {
		this.driver = Objects.requireNonNull(driver);
		this.user = Objects.requireNonNull(user);

		this.mapSecrets();
	}

	private void mapSecrets() {
		JSON secrets = this.user.getSecrets();
		if (secrets == null) {
			this.user.setSecrets(JSON.json("{}"));
			return;
		}

		String raw = secrets.data();
		JsonElement parsed = gson.fromJson(raw, JsonElement.class);
		if (!parsed.isJsonObject()) {
			this.user.setSecrets(JSON.json("{}"));
			return;
		}

		JsonObject object = parsed.getAsJsonObject();
		for (Map.Entry<String, JsonElement> entries : object.entrySet()) {
			String id = entries.getKey();
			JsonElement value = entries.getValue();
			if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
				continue;
			}

			String secret = value.getAsString();
			try {
				NamespacedIdentity identity = NamespacedIdentity.parse(id);
				this.mappedSecrets.put(identity, secret);
			} catch (IllegalArgumentException _) {}
		}
	}

	/**
	 * Get the stored account UUID. Always represents
	 * an offline-mode UUID
	 *
	 * @return the account UUID
	 */
	@Override
	public @NotNull UUID uuid() {
		return this.user.getUuid();
	}

	/**
	 * Update the account UUID
	 *
	 * @param id the new account UUID
	 */
	@Override
	public void uuid(final @NotNull UUID id) {
		this.user.setUuid(id);
	}

	/**
	 * Get the account username
	 *
	 * @return the account owner username
	 */
	@Override
	public @NotNull String username() {
		return this.user.getUsername();
	}

	/**
	 * Set the account username
	 *
	 * @param username the username
	 */
	@Override
	public void username(final @NotNull String username) {
		this.user.setUsername(username);
	}

	/**
	 * Get a secret
	 *
	 * @param id the secret id
	 * @return the secret
	 */
	@Override
	public Optional<String> get(final @NotNull NamespacedIdentity id) {
		return Optional.ofNullable(this.mappedSecrets.get(Objects.requireNonNull(id)));
	}

	/**
	 * Put a secret into the account
	 *
	 * @param id     the secret ID
	 * @param secret the secret value
	 */
	@Override
	public void put(final @NotNull NamespacedIdentity id, final @NotNull String secret) {
		this.mappedSecrets.put(id, secret);
	}

	/**
	 * Removes a secret from the account
	 *
	 * @param id the secret ID
	 */
	@Override
	public void remove(final @NotNull NamespacedIdentity id) {
		this.mappedSecrets.remove(Objects.requireNonNull(id));
	}

	/**
	 * Get when the account was lastly disconnected
	 * at. As the first time a user joins the server he
	 * has no disconnect records for the plugin, this
	 * method might not always return a valid value.
	 *
	 * @return when the account was disconnected from
	 * the server
	 */
	@Override
	public Optional<LocalDateTime> lastDisconnectAt() {
		return Optional.ofNullable(this.user.getLastDisconnectAt());
	}

	/**
	 * Set when the account was lastly disconnected
	 *
	 * @param time the disconnect time
	 */
	@Override
	public void lastDisconnectAt(final @NotNull LocalDateTime time) {
		this.user.setLastDisconnectAt(time);
	}

	/**
	 * Get when the account was last seen
	 * active
	 *
	 * @return the last time the account was
	 * active
	 */
	@Override
	public @NotNull LocalDateTime lastSeenAt() {
		return this.user.getLastSeenAt();
	}

	/**
	 * Set when the account was last seen
	 * active
	 *
	 * @param time the last time seen active
	 */
	@Override
	public void lastSeenAt(final @NotNull LocalDateTime time) {
		this.user.setLastSeenAt(time);
	}

	/**
	 * Get when the account was first seen
	 * active. This is not necessarily equal
	 * to when the account was registered
	 *
	 * @return when the account was first
	 * seen in the server
	 */
	@Override
	public @NotNull LocalDateTime firstSeenAt() {
		return this.user.getFirstSeenAt();
	}

	/**
	 * Set when the account was first seen
	 * active
	 *
	 * @param time when the account was first
	 *             seen
	 */
	@Override
	public void firstSeenAt(final @NotNull LocalDateTime time) {
		this.user.setFirstSeenAt(time);
	}

	/**
	 * Get when the account issued its
	 * registration request. As an account is not
	 * necessarily registered, this method can still
	 * return an empty optional, indicating the account
	 * is not registered
	 *
	 * @return when the account was registered
	 */
	@Override
	public @NotNull Optional<LocalDateTime> registeredAt() {
		return Optional.ofNullable(this.user.getRegisteredAt());
	}

	/**
	 * Set when the account issued its
	 * registration request
	 *
	 * @param time the registration time
	 */
	@Override
	public void registeredAt(final @NotNull LocalDateTime time) {
		this.user.setRegisteredAt(time);
	}

	/**
	 * Save the account in the persistence
	 * storage
	 *
	 * @return the save task
	 */
	@Override
	public @NotNull CompletableFuture<Void> save() {
		JsonObject object = new JsonObject();
		for (Map.Entry<NamespacedIdentity, String> entry : this.mappedSecrets.entrySet()) {
			NamespacedIdentity key = entry.getKey();
			String secret = entry.getValue();

			object.addProperty(key.toString(), secret);
		}

		String raw = gson.toJson(object);
		this.user.setSecrets(JSON.json(raw));

		return this.driver.execute(() -> this.user.update());
	}
}
