package com.github.karmadeb.locklogin.neo.common.user;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * NeoAuth dirty checking user account
 */
class DirtyUserAccount {

	public static final String FIELD_UUID = "uuid";
	public static final String FIELD_USERNAME = "username";
	public static final String FIELD_DISCONNECT = "last_disconnect_at";
	public static final String FIELD_LAST_SEEN = "last_seen_at";
	public static final String FIELD_FIRST_SEEN = "first_seen_at";
	public static final String FIELD_REGISTERED = "registered_at";
	public static final String FIELD_SECRETS = "secrets";

	private final Set<String> fields = new LinkedHashSet<>();

	private UUID uuid;
	private String username;
	private LocalDateTime lastDisconnectAt;
	private LocalDateTime lastSeenAt;
	private LocalDateTime firstSeenAt;
	private LocalDateTime registeredAt;

	public Optional<UUID> uuid() {
		return Optional.ofNullable(this.uuid);
	}

	public void uuid(final UUID id) {
		this.uuid = id;
		fields.add("uuid");
	}

	public Optional<String> username() {
		return Optional.ofNullable(this.username);
	}

	public void username(final String username) {
		this.username = username;
		fields.add("username");
	}

	public Optional<LocalDateTime> lastDisconnectedAt() {
		return Optional.ofNullable(this.lastDisconnectAt);
	}

	public void lastDisconnectedAt(final LocalDateTime time) {
		this.lastDisconnectAt = time;
		fields.add("last_disconnect_at");
	}

	public Optional<LocalDateTime> lastSeenAt() {
		return Optional.ofNullable(this.lastSeenAt);
	}

	public void lastSeenAt(final LocalDateTime time) {
		this.lastSeenAt = time;
		fields.add("last_seen_at");
	}

	public Optional<LocalDateTime> firstSeenAt() {
		return Optional.ofNullable(this.firstSeenAt);
	}

	public void firstSeenAt(final LocalDateTime time) {
		this.firstSeenAt = time;
		fields.add("first_seen_at");
	}

	public Optional<LocalDateTime> registeredAt() {
		return Optional.ofNullable(this.registeredAt);
	}

	public void registeredAt(final LocalDateTime time) {
		this.registeredAt = time;
		fields.add("registered_at");
	}

	public void touchSecrets() {
		fields.add("secrets");
	}

	public void apply(final NeoUserAccount account) {
		if (this.uuid != null) {
			account.uuid = this.uuid;
			this.uuid = null;
		}

		if (this.username != null) {
			account.username = this.username;
			this.username = null;
		}

		if (this.lastDisconnectAt != null) {
			account.lastDisconnectAt = this.lastDisconnectAt;
			this.lastDisconnectAt = null;
		}

		if (this.lastSeenAt != null) {
			account.lastSeenAt = this.lastSeenAt;
			this.lastSeenAt = null;
		}

		if (this.firstSeenAt != null) {
			account.firstSeenAt = this.firstSeenAt;
			this.firstSeenAt = null;
		}

		if (this.registeredAt != null) {
			account.registeredAt = this.registeredAt;
			this.registeredAt = null;
		}
	}

	public String[] fields() {
		return this.fields.toArray(new String[0]);
	}
}
