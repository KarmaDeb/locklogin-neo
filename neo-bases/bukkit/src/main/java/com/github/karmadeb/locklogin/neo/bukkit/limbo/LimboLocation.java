package com.github.karmadeb.locklogin.neo.bukkit.limbo;

import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.UUID;

public final class LimboLocation {

	private static final World world;

	private final NamespacedKey locationKey;
	private final Player player;

	static {
		LimboWorldBiomeProvider provider = new LimboWorldBiomeProvider();
		LimboWorldChunkGenerator generator = new LimboWorldChunkGenerator();

		WorldCreator creator = WorldCreator
				.name("locklogin-limbo")
				.biomeProvider(provider)
				.environment(Environment.CUSTOM)
				.generateStructures(false)
				.hardcore(true)
				.type(WorldType.FLAT)
				.generator(generator)
				.seed(0L);

		world = Bukkit.createWorld(creator);
		if (world == null) {
			throw new IllegalStateException("Failed to create locklogin limbo world");
		}

		world.setSpawnLocation(0, world.getMaxHeight(), 0);
		world.setDifficulty(Difficulty.PEACEFUL);
		world.setTime(12 * 1000);
		world.setGameRule(GameRule.ADVANCE_TIME, false);
		world.setGameRule(GameRule.KEEP_INVENTORY, true);
		world.setSpawnFlags(false, false);
		world.getWorldBorder().setCenter(0, 0);
		world.getWorldBorder().setSize(16);
		world.setChunkForceLoaded(0, 0, true);
	}

	public LimboLocation(final JavaPlugin plugin, final Player player) {
		this.locationKey = NamespacedKey.fromString("locklogin-last-location", plugin);
		this.player = player;
	}

	public Location limbo() {
		return world.getSpawnLocation();
	}

	public void save(final @NotNull Location location) {
		World preWorld = location.getWorld();
		assert preWorld != null;

		if (!preWorld.getUID().equals(world.getUID())) {
			PersistentDataContainer container = player.getPersistentDataContainer();
			container.set(locationKey, LocationDataType.TYPE, location);
		}

		player.teleport(world.getSpawnLocation(), TeleportCause.PLUGIN);
	}

	public void restore() {
		World currWorld = player.getWorld();
		if (currWorld.getUID().equals(world.getUID())) {
			return;
		}

		PersistentDataContainer container = player.getPersistentDataContainer();
		Location location = container.get(locationKey, LocationDataType.TYPE);
		container.remove(locationKey);

		if (location == null) {
			Bukkit.getLogger().warning("Failed to load player " + this.player.getName() +
					" last location. Using world default");

			location = Bukkit.getWorlds().getFirst().getSpawnLocation();
		}

		player.teleport(location, TeleportCause.PLUGIN);
	}

	private static final class LocationDataType implements PersistentDataType<String, Location> {

		public static final LocationDataType TYPE = new LocationDataType();

		/**
		 * Returns the primitive data type of this tag.
		 *
		 * @return the class
		 */
		@Override
		public @NonNull Class<String> getPrimitiveType() {
			return String.class;
		}

		/**
		 * Returns the complex object type the primitive value resembles.
		 *
		 * @return the class type
		 */
		@Override
		public @NonNull Class<Location> getComplexType() {
			return Location.class;
		}

		/**
		 * Returns the primitive data that resembles the complex object passed to
		 * this method.
		 *
		 * @param complex the complex object instance
		 * @param context the context this operation is running in
		 * @return the primitive value
		 */
		@Override
		public @NonNull String toPrimitive(final @NonNull Location complex, final @NonNull PersistentDataAdapterContext context) {
			World world = complex.getWorld();
			if (world == null) {
				throw new IllegalStateException("Cannot store location with invalid world");
			}

			return String.format("%s,%f,%f,%f,%f,%f",
					world.getUID(),
					complex.getX(),
					complex.getY(),
					complex.getZ(),
					complex.getYaw(),
					complex.getPitch());
		}

		/**
		 * Creates a complex object based of the passed primitive value
		 *
		 * @param primitive the primitive value
		 * @param context   the context this operation is running in
		 * @return the complex object instance
		 */
		@Override
		public @NonNull Location fromPrimitive(final @NonNull String primitive, final @NonNull PersistentDataAdapterContext context) {
			String[] parts = primitive.split(",");

			UUID worldUID = UUID.fromString(parts[0]);
			double x = Double.parseDouble(parts[1]);
			double y = Double.parseDouble(parts[2]);
			double z = Double.parseDouble(parts[3]);
			float yaw = Float.parseFloat(parts[4]);
			float pitch = Float.parseFloat(parts[5]);

			World world = Bukkit.getWorld(worldUID);
			if (world == null) {
				Bukkit.getLogger().warning("Failed to load world with UUID " + worldUID +
						" from stored location. Using default world spawn location");
				return Bukkit.getWorlds().getFirst().getSpawnLocation();
			}

			Location loc = new Location(world, x, y, z);
			loc.setYaw(yaw);
			loc.setPitch(pitch);

			return loc;
		}
	}
}
