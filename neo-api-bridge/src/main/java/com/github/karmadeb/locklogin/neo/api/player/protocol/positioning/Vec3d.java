package com.github.karmadeb.locklogin.neo.api.player.protocol.positioning;

import org.jetbrains.annotations.ApiStatus.Experimental;

/**
 * Represents a 3d vector in a world
 * @param x the X position
 * @param y the Y position
 * @param z the Z position
 */
@Experimental
public record Vec3d(double x, double y, double z) {

	/**
	 * Create a new vector
	 * @param x the X position
	 * @param y the Y position
	 * @param z the Z position
	 * @return the vector
	 */
	public static Vec3d of(final double x, final double y, final double z) {
		return new Vec3d(x, y, z);
	}
}
