package com.github.karmadeb.locklogin.neo.api.player.protocol.positioning;

import org.jetbrains.annotations.ApiStatus.Experimental;

/**
 * Represents a 2d vector
 * @param yaw the yaw vector
 * @param pitch the pitch vector
 */
@Experimental
public record Vec2d(float yaw, float pitch) {

	/**
	 * Create a new vector
	 * @param yaw the yaw vector
	 * @param pitch the pitch vector
	 * @return the vector
	 */
	public static Vec2d of(final float yaw, final float pitch) {
		return new Vec2d(yaw, pitch);
	}
}
