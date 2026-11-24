package com.github.karmadeb.locklogin.neo.bukkit.limbo;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public final class LimboWorldChunkGenerator extends ChunkGenerator {

	@Override
	public void generateSurface(@NotNull WorldInfo worldInfo,
								@NotNull Random random,
								int chunkX,
								int chunkZ,
								@NotNull ChunkData chunkData) {
		if (chunkX == 0 && chunkZ == 0) {
			chunkData.setBlock(0, worldInfo.getMaxHeight() - 3, 0, Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("barrier")));
		}
	}
}
