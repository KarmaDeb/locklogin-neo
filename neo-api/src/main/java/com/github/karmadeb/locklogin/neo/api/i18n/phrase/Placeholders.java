package com.github.karmadeb.locklogin.neo.api.i18n.phrase;

import com.github.karmadeb.locklogin.neo.api.MultiSingleFunction;
import com.github.karmadeb.locklogin.neo.api.SneakyFunction;
import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The placeholders helper class allows to fluently
 * build a placeholder phrase and apply it automatically
 */
public final class Placeholders implements MultiSingleFunction<Phrase, String> {

	private static final Map<Class<?>, Function<Object, Object>> PARSERS = new ConcurrentHashMap<>();
	private static final Map<Predicate<Object>, Function<Object, Object>> GENERIC_PARSERS = new ConcurrentHashMap<>();

	private final Map<String, Object> items = new ConcurrentHashMap<>();

	/**
	 * Register a new parser for specific object types. The
	 * object type must be specific, meaning super class or
	 * implementations are not considered
	 * @param type the object class
	 * @param parser the object parser
	 * @param <T> the object type
	 */
	@Contract("null, _ -> fail; _, null -> fail")
	@SuppressWarnings("unchecked")
	public static <T> void registerParser(final Class<T> type, final SneakyFunction<T, Object> parser) {
		PARSERS.put(Objects.requireNonNull(type),
				(Function<Object, Object>) Objects.requireNonNull(parser).normalize());
	}

	/**
	 * Register a new generic parser for any object type
	 * matching the predicate. Unlike {@link #registerParser(Class, SneakyFunction)}
	 * this method takes the last preference for choosing a parser,
	 * and it will try and match any object the predicate allows
	 * @param predicate the tester
	 * @param parser the parser
	 * @param <T> the object type
	 */
	@Contract("null, _ -> fail; _, null -> fail")
	@SuppressWarnings("unchecked")
	public static <T> void registerParser(final Predicate<Object> predicate, final SneakyFunction<T, Object> parser) {
		GENERIC_PARSERS.put(Objects.requireNonNull(predicate),
				(Function<Object, Object>) Objects.requireNonNull(parser).normalize());
	}

	private Placeholders() {}

	/**
	 * Append a placeholder item
	 * @param key the item key
	 * @param item the item value
	 * @return the item
	 */
	public Placeholders with(final String key, final Object item) {
		this.items.put(key, item);
		return this;
	}

	/**
	 * Create a new placeholders parser
	 * @return the parser
	 */
	public static Placeholders parser() {
		return new Placeholders();
	}

	/**
	 * Applies this function to the given argument.
	 *
	 * @param phrases the function argument
	 * @return the function result
	 */
	@Override
	@Contract("null -> fail")
	public String[] apply(final Phrase[] phrases) {
		Objects.requireNonNull(phrases);

		String[] result = new String[phrases.length];
		for (int i = 0; i < phrases.length; i++) {
			Phrase phrase = phrases[i];
			result[i] = this.apply(phrase);
		}

		return result;
	}

	/**
	 * Execute the function
	 *
	 * @param phrase the function parameter
	 * @return the function result
	 */
	@Override
	@Contract("null -> fail")
	public String apply(final Phrase phrase) {
		Objects.requireNonNull(phrase);

		String raw = phrase.message();
		for (String placeholder : phrase.placeholders()) {
			if (!items.containsKey(placeholder)) {
				continue;
			}

			Object value = items.get(placeholder);
			Class<?> type = value.getClass();
			Function<Object, Object> parser = PARSERS.get(type);
			if (parser == null) {
				for (Map.Entry<Predicate<Object>, Function<Object, Object>> entry : GENERIC_PARSERS.entrySet()) {
					Predicate<Object> test = entry.getKey();
					Function<Object, Object> op = entry.getValue();
					if (!test.test(value)) {
						continue;
					}

					parser = op;
					break;
				}
			}

			if (parser != null) {
				value = parser.apply(value);
			} else if (value.getClass().isArray()) {
				value = Arrays.deepToString((Object[]) value);
			}

			raw = raw.replace(placeholder, String.valueOf(value));
		}

		return raw;
	}
}
