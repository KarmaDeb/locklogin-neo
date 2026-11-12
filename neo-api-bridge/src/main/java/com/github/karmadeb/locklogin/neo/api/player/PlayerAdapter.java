package com.github.karmadeb.locklogin.neo.api.player;

//  		        	NOTICE ON EXPERIMENTAL PACKAGE
//
//  This class pertains to locklogin neo-api-bridge. This package is currently
//  experimental, and its methods, structure can change over time without notice
//  nor deprecation notice. For deprecated methods in experimental packages, expect
//  them to be completely removed on next release.
//
//  LockLogin and its author KarmaDev is not responsible for an extension stopped working
//  after an experimental change, as it should be the developer concern to follow the set of
//  changes prepared for the next release and adapt the code accordingly. API updates are always
//  released 1 week before product release, which is considered to be enough time to prepare the
//  product for the upcoming changes.
//
//  In summary:
//  - This package API is not official, but experimental and subject to breaking changes
//  - API might change without deprecation notice
//  - For deprecated API notices, its life span is strictly of 1 release
//  - LockLogin is not responsible for breaking an extension using experimental API. As we provide 1 week grace period
//    before official release. Issues will be ignored and closed without response
//  - LTS is not warranted on experimental API, it might get abandoned, removed, but also implemented into
//    the stable API

import com.github.karmadeb.locklogin.neo.api.player.protocol.messaging.ActionbarMessage;
import com.github.karmadeb.locklogin.neo.api.player.protocol.messaging.TitleMessage;
import com.github.karmadeb.locklogin.neo.api.player.protocol.positioning.Position;
import com.github.karmadeb.locklogin.neo.api.player.protocol.positioning.Vec2d;
import com.github.karmadeb.locklogin.neo.api.player.protocol.positioning.Vec3d;
import com.github.karmadeb.locklogin.neo.api.player.protocol.positioning.WorldIdentifier;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * The player adapter purpose is to serve as a centralized
 * player management. The methods provided by the adapter are
 * highly reduced in comparison to their originals (i.e: Bukkit Player),
 * but are useful when developing multi-platform extensions, however
 * separate implementations is still the highly and only official
 * recommendation.
 * <br/>
 * The neo-api-bridge API might either disappear in
 * the future, be abandoned, or by a miracle, be considered as no longer
 * experimental. All returning methods in adapters will be provided as
 * Optional, the only reason behind this is that when adapting multiple
 * platforms, we cannot currently provide a real contract in where we
 * can ensure that X method will never return null.
 * <br/>
 * Returning everything as non-null Optional is a safer way to deal with
 * this caveat
 */
@Experimental
public interface PlayerAdapter {

	/**
	 * Get the adapted type
	 * @return the adapted type
	 */
	@NotNull
	Class<?> adapter();

	/**
	 * Get the player name
	 * @return the player name
	 */
	@NotNull
	Optional<String> getName();

	/**
	 * Get the player unique ID
	 * @return the UUID of the player
	 */
	@NotNull
	Optional<UUID> getUniqueId();

	/**
	 * Send a message to the player
	 * @param message the message to send
	 */
	void sendMessage(final @NotNull String message);

	/**
	 * Send a message to the player
	 * @param message the message to send
	 * @param messages the additional messages to send, each
	 *                 one in a new line
	 */
	void sendMessage(final @NotNull String message, final @NotNull String... messages);

	/**
	 * Send a title message
	 * @param message the message
	 */
	void sendMessage(final @NotNull TitleMessage message);

	/**
	 * Send an actionbar
	 * @param message the message
	 */
	void sendMessage(final @NotNull ActionbarMessage message);

	/**
	 * Disconnect a client. If no reason is provided, the
	 * operation will not be performed
	 * @param reason the disconnect reason
	 */
	void disconnect(final @NotNull String reason);

	/**
	 * Move the client
	 * @param world the target world
	 * @deprecated use {@link #moveTo(WorldIdentifier)} instead
	 */
	@Deprecated
	default void moveTo(final @NotNull String world) {
		moveTo(WorldIdentifier.of(world));
	}

	/**
	 * Move the client
	 * @param world the target world
	 * @deprecated use {@link #moveTo(WorldIdentifier)} instead
	 */
	@Deprecated
	default void moveTo(final @NotNull UUID world) {
		moveTo(WorldIdentifier.of(world));
	}

	/**
	 * Move the client
	 * @param world the target world
	 * @param vector the vector
	 * @deprecated use {@link #moveTo(WorldIdentifier, Vec3d)} instead
	 */
	@Deprecated
	default void moveTo(final @NotNull String world, final @NotNull Vec3d vector) {
		moveTo(WorldIdentifier.of(world), vector);
	}

	/**
	 * Move the client
	 * @param world the target world
	 * @param vector the vector
	 * @deprecated use {@link #moveTo(WorldIdentifier, Vec3d)} instead
	 */
	@Deprecated
	default void moveTo(final @NotNull UUID world, final @NotNull Vec3d vector) {
		moveTo(WorldIdentifier.of(world), vector);
	}

	/**
	 * Move the client
	 * @param world the target world
	 * @param vector the vector
	 * @param dir the vector
	 * @deprecated use {@link #moveTo(WorldIdentifier, Vec3d, Vec2d)} instead
	 */
	@Deprecated
	default void moveTo(final @NotNull String world, final @NotNull Vec3d vector, final @NotNull Vec2d dir) {
		moveTo(WorldIdentifier.of(world), vector, dir);
	}

	/**
	 * Move the client
	 * @param world the target world
	 * @param vector the vector
	 * @param dir the vector
	 * @deprecated use {@link #moveTo(WorldIdentifier, Vec3d, Vec2d)} instead
	 */
	@Deprecated
	default void moveTo(final @NotNull UUID world, final @NotNull Vec3d vector, final @NotNull Vec2d dir) {
		moveTo(WorldIdentifier.of(world), vector, dir);
	}

	/**
	 * Move the client
	 * @param world the target world
	 */
	void moveTo(final @NotNull WorldIdentifier world);

	/**
	 * Move the client
	 * @param world the target world
	 * @param vector the vector
	 */
	void moveTo(final @NotNull WorldIdentifier world, final @NotNull Vec3d vector);

	/**
	 * Move the client
	 * @param world the target world
	 * @param vector the vector
	 * @param dir the vector
	 */
	void moveTo(final @NotNull WorldIdentifier world, final @NotNull Vec3d vector, final @NotNull Vec2d dir);

	/**
	 * Move the client
	 * @param vector the vector
	 */
	void moveTo(final @NotNull Vec3d vector);

	/**
	 * Move the client
	 * @param vector the vector
	 * @param dir the direction
	 */
	void moveTo(final @NotNull Vec3d vector, final @NotNull Vec2d dir);

	/**
	 * Move the client
	 * @param position the new position
	 */
	void moveTo(final @NotNull Position position);

	/**
	 * Get the underlying player around the player
	 * adapter.
	 * @return the underlying player
	 */
	@NotNull
	Optional<Object> getUnderlyingPlayer();

	/**
	 * Player adapter specific auth layer. Not really required to extend
	 * from this.
	 * Even though this is experimental, the implementation of {@link AuthSession}
	 * checks if the {@link #type()} is of type {@link PlayerAdapter}, registering
	 * the layer on all platforms.
	 * Keep in mind that, as mentioned in {@link AuthLayer} documentation, the layer
	 * is registered automatically only if it's added into the registry, otherwise it
	 * must be added manually during session pipeline build phase
	 */
	@Experimental
	abstract class Layer implements AuthLayer<PlayerAdapter> {

		/**
		 * Get the layer type. This is essentially required
		 * for in-runtime validation of player object type
		 * before calling to {@link #challenge(Object)}. It's important
		 * to specify the object type correctly, for instance
		 * {@code org.bukkit.entity.Player.class} for Bukkit.
		 * Alternatively, you can use the experimental "neo-api-bridge"
		 * API to have a "centralized" way of interacting with players
		 * and the server object, even though it is not currently recommended
		 *
		 * @return the layer type
		 */
		@Override
		public final @NotNull Class<PlayerAdapter> type() {
			return PlayerAdapter.class;
		}
	}
}
