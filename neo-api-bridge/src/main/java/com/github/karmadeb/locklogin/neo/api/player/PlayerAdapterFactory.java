package com.github.karmadeb.locklogin.neo.api.player;

import org.jetbrains.annotations.ApiStatus.Experimental;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Represents a factory for creating player adapters
 * from different player types
 */
@Experimental
public final class PlayerAdapterFactory {

	private static final Map<Class<?>, Function<Object, PlayerAdapter>> adapters = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Class<?>> proxies = new ConcurrentHashMap<>();

	/**
	 * Register a new player type adapter
	 * @param type the player type
	 * @param adapter the adapter
	 * @param <T> the player type
	 */
	@SuppressWarnings("unchecked")
	public static <T> void register(final Class<T> type, final Function<T, PlayerAdapter> adapter) {
		adapters.put(type, (Function<Object, PlayerAdapter>) adapter);
	}

	/**
	 * Adapt a player
	 * @param player the player to adapt
	 * @return the adapted player
	 */
	public static Optional<PlayerAdapter> adapt(final Object player) {
		if (player == null) {
			return Optional.empty();
		}

		Class<?> type = proxies.getOrDefault(player.getClass(), player.getClass());
		Function<Object, PlayerAdapter> adapter = adapters.getOrDefault(type, navigate(type));
		if (adapter == null) {
			return Optional.empty();
		}

		return Optional.ofNullable(adapter.apply(player));
	}

	private static Function<Object, PlayerAdapter> navigate(final Class<?> type) {
		for (Map.Entry<Class<?>, Function<Object, PlayerAdapter>> entry : adapters.entrySet()) {
			Class<?> clazz = entry.getKey();
			Function<Object, PlayerAdapter> adapter = entry.getValue();
			if (clazz.isAssignableFrom(type) && adapter != null) {
				proxies.put(type, clazz);
				return adapter;
			}
		}

		return null;
	}
}
