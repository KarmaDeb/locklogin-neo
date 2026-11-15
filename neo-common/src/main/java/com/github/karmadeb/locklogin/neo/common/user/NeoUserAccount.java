package com.github.karmadeb.locklogin.neo.common.user;

import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.persistence.driver.PersistenceDriver.FormatTarget;
import com.github.karmadeb.locklogin.neo.api.persistence.driver.ResultSetDriver;
import com.github.karmadeb.locklogin.neo.api.user.account.Account;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NeoUserAccount implements Account {

	private static final Gson gson = new GsonBuilder().create();

	protected final ResultSetDriver driver;
	protected final Map<NamespacedIdentity, String> secrets = new ConcurrentHashMap<>();
	protected UUID uuid;
	protected String username;
	protected LocalDateTime lastDisconnectAt;
	protected LocalDateTime lastSeenAt;
	protected LocalDateTime firstSeenAt;
	protected LocalDateTime registeredAt;
	private boolean isNewRecord = true;

	private final DirtyUserAccount dirty = new DirtyUserAccount();

	@Contract("null -> fail")
	public NeoUserAccount(final ResultSetDriver driver) {
		this.driver = Objects.requireNonNull(driver);
	}

	public void init(final UUID uuid,
					 final String username,
					 final LocalDateTime disconnectAt,
					 final LocalDateTime lastSeen,
					 final LocalDateTime firstSeen,
					 final LocalDateTime registeredAt,
					 final Map<NamespacedIdentity, String> secrets) {
		isNewRecord = false;

		this.uuid = uuid;
		this.username = username;
		this.lastDisconnectAt = disconnectAt;
		this.lastSeenAt = lastSeen;
		this.firstSeenAt = firstSeen;
		this.registeredAt = registeredAt;
		this.secrets.putAll(secrets);
	}

	/**
	 * Get the stored account UUID. Always represents
	 * an offline-mode UUID
	 *
	 * @return the account UUID
	 */
	@Override
	public @NotNull UUID uuid() {
		return this.dirty.uuid().orElse(this.uuid);
	}

	/**
	 * Update the account UUID
	 *
	 * @param id the new account UUID
	 */
	@Override
	public void uuid(final @NotNull UUID id) {
		this.dirty.uuid(Objects.requireNonNull(id));
	}

	/**
	 * Get the account username
	 *
	 * @return the account owner username
	 */
	@Override
	public @NotNull String username() {
		return this.dirty.username().orElse(this.username);
	}

	/**
	 * Set the account username
	 *
	 * @param username the username
	 */
	@Override
	public void username(final @NotNull String username) {
		this.dirty.username(Objects.requireNonNull(username));
	}

	/**
	 * Get a secret
	 *
	 * @param id the secret id
	 * @return the secret
	 */
	@Override
	public Optional<String> get(final @NotNull NamespacedIdentity id) {
		return Optional.ofNullable(this.secrets.get(Objects.requireNonNull(id)));
	}

	/**
	 * Put a secret into the account
	 *
	 * @param id     the secret ID
	 * @param secret the secret value
	 */
	@Override
	public void put(final @NotNull NamespacedIdentity id, final @NotNull String secret) {
		this.dirty.touchSecrets();
		this.secrets.put(Objects.requireNonNull(id), Objects.requireNonNull(secret));
	}

	/**
	 * Removes a secret from the account
	 *
	 * @param id the secret ID
	 */
	@Override
	public void remove(final @NotNull NamespacedIdentity id) {
		this.dirty.touchSecrets();
		this.secrets.remove(Objects.requireNonNull(id));
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
		return this.dirty.lastDisconnectedAt().or(() -> Optional.ofNullable(this.lastDisconnectAt));
	}

	/**
	 * Set when the account was lastly disconnected
	 *
	 * @param time the disconnect time
	 */
	@Override
	public void lastDisconnectAt(final @NotNull LocalDateTime time) {
		this.dirty.lastDisconnectedAt(Objects.requireNonNull(time));
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
		return this.dirty.lastSeenAt().orElse(this.lastSeenAt);
	}

	/**
	 * Set when the account was last seen
	 * active
	 *
	 * @param time the last time seen active
	 */
	@Override
	public void lastSeenAt(final @NotNull LocalDateTime time) {
		this.dirty.lastSeenAt(Objects.requireNonNull(time));
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
		return this.dirty.firstSeenAt().orElse(this.firstSeenAt);
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
		this.dirty.firstSeenAt(Objects.requireNonNull(time));
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
		return this.dirty.registeredAt().or(() -> Optional.ofNullable(this.registeredAt));
	}

	/**
	 * Set when the account issued its
	 * registration request
	 *
	 * @param time the registration time
	 */
	@Override
	public void registeredAt(final @NotNull LocalDateTime time) {
		this.dirty.registeredAt(Objects.requireNonNull(time));
	}

	/**
	 * Save the account in the persistence
	 * storage
	 *
	 * @return the save task
	 */
	@Override
	public @NotNull CompletableFuture<Void> save() {
		UUID uuid = this.uuid();
		String username = this.username();
		LocalDateTime lastSeen = this.lastSeenAt();
		LocalDateTime firstSeen = this.firstSeenAt();

		Objects.requireNonNull(uuid, "Cannot save account with null UUID");
		Objects.requireNonNull(username, "Cannot save account with null username");
		Objects.requireNonNull(lastSeen, "Cannot save account with null lastSeenAt");
		Objects.requireNonNull(firstSeen, "Cannot save account with null firstSeenAt");

		String[] fields = this.dirty.fields();
		if (fields.length == 0) {
			return CompletableFuture.completedFuture(null);
		}

		String tableName = this.driver.format("users", FormatTarget.TABLE_NAME);
		String[] fieldNames = Arrays.stream(fields)
				.map(name -> driver.format(name, FormatTarget.FIELD_NAME))
				.toArray(String[]::new);

		if (isNewRecord) {
			String query = String.format("INSERT INTO %s (%s) VALUES (%s)",
					tableName,
					String.join(", ", fieldNames),
					new StringBuilder().repeat('?', fieldNames.length));

			return this.driver.execute(connection -> {
				try (PreparedStatement statement = connection.prepareStatement(query)) {
					mapToStatement(statement, fields);
					statement.execute();

					this.dirty.apply(this);
				}
			});
		}

		String query = String.format("UPDATE %s SET %s = ? WHERE uuid = ?",
				tableName,
				String.join(", = ?", fieldNames)
		);

		return this.driver.execute(connection -> {
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				mapToStatement(statement, fields);

				statement.setString(fields.length + 1, this.uuid.toString());
				statement.executeUpdate();

				this.dirty.apply(this);
			}
		});
	}

	private void mapToStatement(final PreparedStatement statement, final String[] fields) throws SQLException {
		for (int index = 1; index <= fields.length; index++) {
			String field = fields[index - 1];
			switch (field) {
				case DirtyUserAccount.FIELD_UUID ->
						statement.setString(index, this.uuid().toString());
				case DirtyUserAccount.FIELD_USERNAME ->
						statement.setString(index, this.username());
				case DirtyUserAccount.FIELD_DISCONNECT -> {
					LocalDateTime time = this.lastDisconnectAt().orElse(null);
					if (time == null) {
						statement.setNull(index, Types.TIMESTAMP);
					} else {
						statement.setTimestamp(index, Timestamp.valueOf(time));
					}
				}
				case DirtyUserAccount.FIELD_LAST_SEEN ->
						statement.setTimestamp(index, Timestamp.valueOf(this.lastSeenAt()));

				case DirtyUserAccount.FIELD_FIRST_SEEN ->
						statement.setTimestamp(index, Timestamp.valueOf(this.firstSeenAt()));

				case DirtyUserAccount.FIELD_REGISTERED -> {
					LocalDateTime time = this.registeredAt().orElse(null);
					if (time == null) {
						statement.setNull(index, Types.TIMESTAMP);
					} else {
						statement.setTimestamp(index, Timestamp.valueOf(time));
					}
				}

				case DirtyUserAccount.FIELD_SECRETS -> {
					JsonObject object = new JsonObject();
					for (Map.Entry<NamespacedIdentity, String> entry : this.secrets.entrySet()) {
						String key = entry.getKey().toString();
						String value = entry.getValue();

						object.addProperty(key, value);
					}

					statement.setString(index, gson.toJson(object));
				}
			}
		}
	}
}
