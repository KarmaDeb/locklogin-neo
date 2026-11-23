package com.github.karmadeb.locklogin.neo.common.config;

import com.github.karmadeb.locklogin.neo.api.configuration.ConfigReader;
import com.github.karmadeb.locklogin.neo.common.NodeResolver;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConfigurationReader implements ConfigReader {

	private static final String PATTERN_INTEGER = "^[-+]?\\d+$";
	private static final String PATTERN_FLOAT = "^-?(\\d+(\\.\\d*)?|\\.\\d+)$";

	private static final Predicate<String> IS_INT = raw -> raw.matches(PATTERN_INTEGER);
	private static final Predicate<String> IS_FLOAT = raw -> raw.matches(PATTERN_FLOAT);

	protected final MappingNode yaml;
	protected final NodeResolver resolver;

	@Contract("null -> fail")
	public ConfigurationReader(final MappingNode yamlDocument) {
		this.yaml = Objects.requireNonNull(yamlDocument);
		this.resolver = new NodeResolver(this.yaml);
	}

	@Internal
	public MappingNode yaml() {
		return this.yaml;
	}

	/**
	 * Get if key exists in the configuration
	 * file
	 *
	 * @param key the key
	 * @return if the key exists
	 */
	@Override
	public boolean has(final String key) {
		return this.resolver.resolve(key).isPresent();
	}

	/**
	 * Get a string value
	 *
	 * @param key the string key
	 * @return the string value
	 */
	@Override
	public Optional<String> getString(final @NotNull String key) {
		Optional<Node> resolved = this.resolver.resolve(key);
		if (resolved.isEmpty()) {
			return Optional.empty();
		}

		Node node = resolved.get();
		if (node instanceof ScalarNode scalar) {
			return Optional.of(scalar.getValue());
		}

		return Optional.empty();
	}

	/**
	 * Get a byte value
	 *
	 * @param key the byte key
	 * @return the byte value
	 */
	@Override
	public Optional<Byte> getByte(final @NotNull String key) {
		return parse(key, IS_INT, Byte::parseByte);
	}

	/**
	 * Get a short value
	 *
	 * @param key the short key
	 * @return the short value
	 */
	@Override
	public Optional<Short> getShort(final @NotNull String key) {
		return parse(key, IS_INT, Short::parseShort);
	}

	/**
	 * Get an int value
	 *
	 * @param key the int key
	 * @return the int value
	 */
	@Override
	public Optional<Integer> getInt(final @NotNull String key) {
		return parse(key, IS_INT, Integer::parseInt);
	}

	/**
	 * Get a long value
	 *
	 * @param key the long key
	 * @return the long value
	 */
	@Override
	public Optional<Long> getLong(final @NotNull String key) {
		return parse(key, IS_INT, Long::parseLong);
	}

	/**
	 * Get a float value
	 *
	 * @param key the float key
	 * @return the float value
	 */
	@Override
	public Optional<Float> getFloat(final @NotNull String key) {
		return parse(key, IS_INT.or(IS_FLOAT), Float::parseFloat);
	}

	/**
	 * Get a double value
	 *
	 * @param key the double key
	 * @return the double value
	 */
	@Override
	public Optional<Double> getDouble(final @NotNull String key) {
		return parse(key, IS_INT.or(IS_FLOAT), Double::parseDouble);
	}

	/**
	 * Get a boolean value
	 *
	 * @param key the boolean key
	 * @return the boolean value
	 */
	@Override
	public Optional<Boolean> getBool(final @NotNull String key) {
		Optional<Node> resolved = this.resolver.resolve(key);
		if (resolved.isEmpty()) {
			return Optional.empty();
		}

		Node node = resolved.get();
		if (node instanceof ScalarNode scalar) {
			String raw = scalar.getValue();
			if (raw.equals("true") || raw.equals("false")) {
				return Optional.of(raw.equals("true"));
			}
		}

		return Optional.empty();
	}

	/**
	 * Get a section value
	 *
	 * @param key the section key
	 * @return the section value
	 */
	@Override
	public Optional<ConfigurationWriter> getSection(final @NotNull String key) {
		Optional<Node> resolved = this.resolver.resolve(key);
		if (resolved.isEmpty()) {
			return Optional.empty();
		}

		Node node = resolved.get();
		if (node instanceof MappingNode mapping) {
			return Optional.of(new ConfigurationWriter(mapping));
		}

		return Optional.empty();
	}

	/**
	 * Get a sections value
	 *
	 * @param key the sections key
	 * @return the sections value
	 */
	@Override
	public Optional<List<ConfigurationReader>> getSections(final @NotNull String key) {
		Optional<Node> resolved = this.resolver.resolve(key);
		if (resolved.isEmpty()) {
			return Optional.empty();
		}

		Node node = resolved.get();
		if (node instanceof SequenceNode sequence) {
			List<ConfigurationReader> nodes = new ArrayList<>();
			for (Node item : sequence.getValue()) {
				if (!(item instanceof MappingNode mapping)) {
					continue;
				}

				nodes.add(new ConfigurationWriter(mapping));
			}

			return Optional.of(nodes);
		}

		return Optional.empty();
	}

	/**
	 * Get a string list value
	 *
	 * @param key the string list key
	 * @return the string list value
	 */
	@Override
	public List<String> getStringList(final @NotNull String key) {
		Optional<Node> resolved = this.resolver.resolve(key);
		if (resolved.isEmpty()) {
			return new ArrayList<>();
		}

		List<String> items = new ArrayList<>();
		Node node = resolved.get();
		if (node instanceof SequenceNode sequence) {
			for (Node item : sequence.getValue()) {
				if (!(item instanceof ScalarNode scalar)) {
					continue;
				}

				items.add(scalar.getValue());
			}
		}

		return items;
	}

	protected final <T> Optional<T> parse(final @NotNull String key,
										  final Predicate<String> check,
										  final Function<String, T> parser) {
		Optional<Node> resolved = this.resolver.resolve(key);
		if (resolved.isEmpty()) {
			return Optional.empty();
		}

		Node node = resolved.get();
		if (node instanceof ScalarNode scalar) {
			String raw = scalar.getValue();
			if (check.test(raw)) {
				return Optional.of(parser.apply(raw));
			}
		}

		return Optional.empty();
	}
}
