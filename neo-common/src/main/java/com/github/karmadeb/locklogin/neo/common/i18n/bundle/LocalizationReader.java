package com.github.karmadeb.locklogin.neo.common.i18n.bundle;

import com.github.karmadeb.locklogin.neo.api.i18n.bundle.LocaleReader;
import com.github.karmadeb.locklogin.neo.api.i18n.phrase.Phrase;
import com.github.karmadeb.locklogin.neo.common.NodeResolver;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.nodes.*;

import java.util.*;

public class LocalizationReader implements LocaleReader {

	protected final Locale locale;
	protected final MappingNode yaml;
	protected final NodeResolver resolver;

	@Contract("null, _ -> fail; _, null -> fail")
	public LocalizationReader(final Locale locale, final MappingNode yamlDocument) {
		this.locale = Objects.requireNonNull(locale);
		this.yaml = Objects.requireNonNull(yamlDocument);
		this.resolver = new NodeResolver(this.yaml);
	}

	@Internal
	public MappingNode yaml() {
		return this.yaml;
	}

	/**
	 * Get the locale
	 *
	 * @return the locale
	 */
	@Override
	public Locale locale() {
		return this.locale;
	}

	/**
	 * Get if a phrase is defined
	 *
	 * @param key the phrase key
	 * @return if the phrase exists
	 */
	@Override
	public boolean defined(final @NotNull String key) {
		return this.resolver.resolve(key).isPresent();
	}

	/**
	 * Find a phrase. If no phrase is found, this method
	 * returns an empty optional
	 *
	 * @param key the phrase key
	 * @return the phrase
	 */
	@Override
	public @NotNull Optional<Phrase[]> find(final @NotNull String key) {
		Optional<Node> resolved = this.resolver.resolve(key);
		if (resolved.isEmpty()) {
			return Optional.empty();
		}

		Node node = resolved.get();
		if (node instanceof ScalarNode scalar) {
			return Optional.of(new Phrase[]{
					new Phrase(this.locale, scalar.getValue())
			});
		}

		if (node instanceof SequenceNode sequence) {
			return Optional.of(sequence.getValue().stream()
					.filter(n -> n instanceof ScalarNode)
					.map(n -> (ScalarNode) n)
					.map(ScalarNode::getValue)
					.map(p -> new Phrase(this.locale, p))
					.toArray(Phrase[]::new));
		}

		return Optional.empty();
	}
}
