package com.github.karmadeb.locklogin.neo.api.configuration.registry;

import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPart;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * The config registry holds all the configuration
 * files and {@link ConfigPart} registered through
 * extensions or by LockLogin itself
 */
public abstract class ConfigRegistry {

	/**
	 * Append a part to a configuration file
	 * @param file the file to append
	 * @param part the part to append
	 */
	public abstract void append(final @NotNull ConfigFile file, final @NotNull ConfigPart part);

	/**
	 * Get all the parts of a configuration file
	 * @param file the file parts
	 * @return the parts
	 */
	public abstract ConfigPart @NotNull[] parts(final @NotNull ConfigFile file);

	/**
	 * Get a part of a configuration file
	 * @param file the file
	 * @param part the file part
	 * @return the configuration file part
	 * @param <T> the part type
	 */
	@NotNull
	public abstract <T extends ConfigPart> Optional<T> part(final @NotNull ConfigFile file, final @NotNull Class<T> part);
}
