package com.github.karmadeb.locklogin.neo.api.extension;

import com.github.karmadeb.locklogin.neo.api.extension.description.ExtensionDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * LockLogin extensions registry
 */
public interface ExtensionManager {

	/**
	 * Tries to load an extension.
	 * @param extension the extension to load
	 * @return the loaded extension
	 */
	Extension load(final @NotNull Class<? extends NeoExtension> extension);

	/**
	 * Enables an extension
	 * @param extension the extension to enable
	 * @return if the extension was enabled
	 */
	boolean enable(final @NotNull Extension extension);

	/**
	 * Disables an extension
	 * @param extension the extension to disable
	 * @return if the extension was disabled
	 */
	boolean disable(final @NotNull Extension extension);

	/**
	 * Get an extension by its extension instance
	 * @param extension the extension
	 * @return the extension, or an empty optional
	 * if not loaded
	 */
	default Optional<Extension> extension(final @NotNull NeoExtension extension) {
		return this.extension(extension.description());
	}

	/**
	 * Get an extension by an extension description
	 * @param description the description
	 * @return the extension, or an empty optional
	 * if not loaded
	 */
	default Optional<Extension> extension(final @NotNull ExtensionDescription description) {
		return this.extension(description.id());
	}

	/**
	 * Get an extension by its ID
	 * @param id the extension ID
	 * @return the extension
	 */
	Optional<Extension> extension(final @NotNull String id);

	/**
	 * Get all the registered extensions
	 * @return the extensions
	 */
	@NotNull @Unmodifiable
	default Collection<? extends Extension> extensions() {
		return this.extensions(_ -> true);
	}

	/**
	 * Get all the registered extensions
	 * @param filter the extension filter
	 * @return the extensions
	 */
	@NotNull @Unmodifiable
	Collection<? extends Extension> extensions(final @NotNull Predicate<? extends Extension> filter);
}
