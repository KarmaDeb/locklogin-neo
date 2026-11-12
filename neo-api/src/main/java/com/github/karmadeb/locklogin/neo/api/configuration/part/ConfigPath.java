package com.github.karmadeb.locklogin.neo.api.configuration.part;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The config path serves as a structured
 * way of dealing with nested config paths
 * without having to get too dizzy
 */
public final class ConfigPath {

	private final List<String> parts = new ArrayList<>();

	/**
	 * Create a new config path
	 * @param parts the path parts
	 */
	@Contract("null -> fail")
	private ConfigPath(final List<String> parts) {
		this.parts.addAll(Objects.requireNonNull(parts));
	}

	/**
	 * Append a part to the path
	 * @param part the part to append
	 * @return the modified config path
	 */
	@Contract("null -> fail")
	public ConfigPath append(final String part) {
		this.parts.add(Objects.requireNonNull(part));
		return this;
	}

	/**
	 * Removes a part from the path
	 * @param part the part to remove
	 * @return the modified config path
	 */
	@Contract("null -> fail")
	public ConfigPath removeFirst(final String part) {
		int firstIndex = this.parts.indexOf(Objects.requireNonNull(part));
		if (firstIndex >= 0) {
			this.parts.remove(firstIndex);
		}
		
		return this;
	}
	
	/**
	 * Removes a part from the path
	 * @param part the part to remove
	 * @return the modified config path
	 */
	@Contract("null -> fail")
	public ConfigPath removeLast(final String part) {
		int lastIndex = this.parts.lastIndexOf(Objects.requireNonNull(part));
		if (lastIndex >= 0) {
			this.parts.remove(lastIndex);
		}

		return this;
	}

	/**
	 * Check if a part is present in the current
	 * path
	 * @param part the part
	 * @return if the part is contained
	 */
	@Contract("null -> fail")
	public boolean contains(final String part) {
		return this.parts.contains(Objects.requireNonNull(part));
	}

	/**
	 * Navigate the path
	 * @return the navigated path
	 */
	public Walker navigate() {
		String[] nodes = this.parts.toArray(new String[0]);
		if (nodes.length == 0) {
			return null;
		}

		int index = nodes.length - 1;

		Walker walker = null;
		String current;
		do {
			current = nodes[index];
			walker = new Walker(current, walker);
		} while (--index >= 0);

		return walker;
	}

	/**
	 * Build the path
	 * @return the result path
	 */
	@NotNull
	public String build() {
		return this.parts.stream()
				.map(s -> s.replace(".", "\\."))
				.collect(Collectors.joining("."));
	}

	/**
	 * Create a new empty config path
	 * @return the path
	 */
	@NotNull
	public static ConfigPath empty() {
		return new ConfigPath(Collections.emptyList());
	}

	/**
	 * Create a new config path
	 * @param part the first part
	 * @param parts the sequenced parts
	 * @return the config path
	 */
	@NotNull @Contract("null, _ -> fail")
	public static ConfigPath of(final String part, final String... parts) {
		List<String> pathParts = new ArrayList<>();
		pathParts.add(part);
		if (parts != null) {
			pathParts.addAll(Arrays.asList(parts));
		}
		
		return new ConfigPath(pathParts);
	}

	public record Walker(String from, Walker to) {}
}
