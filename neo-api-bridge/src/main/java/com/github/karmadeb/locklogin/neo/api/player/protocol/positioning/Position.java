package com.github.karmadeb.locklogin.neo.api.player.protocol.positioning;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a position in a world. Including a 3d vector
 * representing the position, and a 2d vector representing the
 * direction
 * @param world the world
 * @param pos the position
 * @param dir the direction
 */
@Experimental
public record Position(@NotNull WorldIdentifier world, @NotNull Vec3d pos, @NotNull Vec2d dir) {

	/**
	 * Create a new position
	 * @param world the world
	 * @param pos the position
	 * @param dir the direction
	 * @deprecated use {@link Position(WorldIdentifier, Vec3d, Vec2d)} instead
	 */
	@Deprecated
	public Position(final @NotNull String world, final @NotNull Vec3d pos, final @NotNull Vec2d dir) {
		this(WorldIdentifier.of(world), pos, dir);
	}

	/**
	 * Create a new position
	 * @param world the world
	 * @param pos the position
	 * @param dir the direction
	 * @return the position
	 */
	public static Position of(final @NotNull WorldIdentifier world, final @NotNull Vec3d pos, final @NotNull Vec2d dir) {
		return new Position(world, pos, dir);
	}

	/**
	 * Create a new position
	 * @param world the world
	 * @param pos the position
	 * @param dir the direction
	 * @return the position
	 * @deprecated use {@link #of(WorldIdentifier, Vec3d, Vec2d)} instead
	 */
	@Deprecated
	public static Position of(final @NotNull String world, final @NotNull Vec3d pos, final @NotNull Vec2d dir) {
		return new Position(world, pos, dir);
	}
}
