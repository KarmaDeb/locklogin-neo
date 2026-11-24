package com.github.karmadeb.locklogin.neo.bukkit.limbo;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class LimboWorldBiomeProvider extends BiomeProvider {

	/**
	 * Return the Biome which should be present at the provided location.
	 * <p>
	 * Notes:
	 * <p>
	 * This method <b>must</b> be completely thread safe and able to handle
	 * multiple concurrent callers.
	 * <p>
	 * This method should only return biomes which are present in the list
	 * returned by {@link #getBiomes(WorldInfo)}
	 * <p>
	 * This method should <b>never</b> return {@link Biome#CUSTOM}.
	 *
	 * @param worldInfo The world info of the world the biome will be used for
	 * @param x         The X-coordinate from world origin
	 * @param y         The Y-coordinate from world origin
	 * @param z         The Z-coordinate from world origin
	 * @return Biome for the given location
	 */
	@Override
	public @NonNull Biome getBiome(final @NonNull WorldInfo worldInfo, int x, int y, int z) {
		return Biome.PLAINS;
	}

	/**
	 * Returns a list with every biome the {@link BiomeProvider} will use for
	 * the given world.
	 * <p>
	 * Notes:
	 * <p>
	 * This method only gets called once, when the world is loaded. Returning
	 * another list or modifying the values from the initial returned list later
	 * one, are not respected.
	 * <p>
	 * This method should <b>never</b> return a list which contains
	 * {@link Biome#CUSTOM}.
	 *
	 * @param worldInfo The world info of the world the list will be used for
	 * @return A list with every biome the {@link BiomeProvider} uses
	 */
	@Override
	public @NonNull List<Biome> getBiomes(@NonNull WorldInfo worldInfo) {
		return List.of(Biome.PLAINS);
	}
}
