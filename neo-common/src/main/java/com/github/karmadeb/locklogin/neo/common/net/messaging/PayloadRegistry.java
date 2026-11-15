package com.github.karmadeb.locklogin.neo.common.net.messaging;

import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.SneakyFunction;
import com.github.karmadeb.locklogin.neo.common.net.buffers.NetBufferReader;
import com.github.karmadeb.locklogin.neo.common.net.messaging.payload.NeoPayload;
import org.jetbrains.annotations.Contract;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class PayloadRegistry {

	private static final Map<NamespacedIdentity, SneakyFunction<NetBufferReader, NeoPayload>> payloads = new ConcurrentHashMap<>();

	@Contract("null, _ -> fail; _, null -> fail")
	public static void register(final NamespacedIdentity payload, SneakyFunction<NetBufferReader, NeoPayload> factory) {
		Objects.requireNonNull(payload);
		Objects.requireNonNull(factory);

		if (payloads.containsKey(payload)) {
			return;
		}

		payloads.put(payload, factory);
	}

	@Contract("null, _ -> fail; _, null -> fail")
	public static Optional<NeoPayload> parse(final NamespacedIdentity type, final NetBufferReader reader) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(reader);

		SneakyFunction<NetBufferReader, NeoPayload> factory = payloads.get(type);
		if (factory == null) {
			return Optional.empty();
		}

		try {
			NeoPayload result = factory.apply(reader);
			return Optional.ofNullable(result);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
