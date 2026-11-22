package com.github.karmadeb.locklogin.neo.api.messaging.protocol;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.messaging.protocol.payload.Codec;
import com.github.karmadeb.locklogin.neo.api.messaging.protocol.payload.Payload;
import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The protocol registry allows to register
 * different payloads and their codecs
 */
@SuppressWarnings("unused")
public final class Protocol {

	private static final Map<NamespacedIdentity, Codec<?>> codecs = new HashMap<>();

	/**
	 * Register a new codec
	 * @param payload the payload to register the codec
	 *                for
	 * @param codec the codec
	 * @param <T> the payload type
	 * @throws Exception if a codec for the same payload id is already
	 * registered
	 */
	@Contract("null, _ -> fail; _, null -> fail")
	public static <T extends Payload> void register(final T payload, final Codec<T> codec) throws Exception {
		Objects.requireNonNull(payload);
		register(payload.id(), codec);
	}

	/**
	 * Register a new codec
	 * @param payload the payload to register the codec
	 *                for
	 * @param codec the codec
	 * @param <T> the payload type
	 * @throws Exception if a codec for the same payload id is already
	 * registered
	 */
	@Contract("null, _ -> fail; _, null -> fail")
	public static <T extends Payload> void register(final NamespacedIdentity payload, final Codec<T> codec) throws Exception {
		Objects.requireNonNull(payload);
		Objects.requireNonNull(codec);

		if (codecs.containsKey(payload)) {
			throw new Exception(String.format("Failed to register codec with id %s (another codec with same ID is already registered)",
					payload));
		}

		codecs.put(payload, codec);
	}

	/**
	 * Get a codec
	 * @param id the codec id
	 * @return the codec
	 * @param <T> the payload type
	 */
	@Contract("null -> fail")
	public static <T extends Payload> Optional<Codec<T>> getCodec(final NamespacedIdentity id) {
		Objects.requireNonNull(id);
		try {
			Codec<?> codec = codecs.get(id);
			// noinspection unchecked, rawtypes
			Codec<T> resolved = (Codec) codec;

			return Optional.ofNullable(resolved);
		} catch (ClassCastException _) {}

		return Optional.empty();
	}
}
