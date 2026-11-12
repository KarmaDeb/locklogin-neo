package com.github.karmadeb.locklogin.neo.api.player.protocol.positioning;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a world identifier. The identifier allows
 * to identify a single word by two methods. World name and
 * World UID
 */
@Experimental
public final class WorldIdentifier {

	private final String name;
	private final UUID uid;

	/**
	 * Create a new world identifier
	 * @param name the world name
	 */
	@Contract("null -> fail")
	public WorldIdentifier(final String name) {
		this.name = Objects.requireNonNull(name);
		this.uid = null;
	}

	/**
	 * Create a new world identifier
	 * @param uid the world UID
	 */
	@Contract("null -> fail")
	public WorldIdentifier(final UUID uid) {
		this.name = null;
		this.uid = Objects.requireNonNull(uid);
	}

	/**
	 * Get the world name
	 * @return the world name
	 */
	@NotNull
	public Optional<String> name() {
		return Optional.ofNullable(this.name);
	}

	/**
	 * Get the world UID
	 * @return the world UID
	 */
	public Optional<UUID> uid() {
		return Optional.ofNullable(this.uid);
	}

	/**
	 * Create a new world identifier
	 * @param name the world name
	 * @return the world identifier
	 */
	@Contract("null -> fail")
	public static WorldIdentifier of(final String name) {
		return new WorldIdentifier(name);
	}

	/**
	 * Create a new world identifier
	 * @param uid the world UID
	 * @return the world identifier
	 */
	@Contract("null -> fail")
	public static WorldIdentifier of(final UUID uid) {
		return new WorldIdentifier(uid);
	}
}
