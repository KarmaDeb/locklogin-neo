package com.github.karmadeb.locklogin.neo.common.network.messaging;

import com.github.karmadeb.locklogin.neo.api.messaging.Destination;
import com.github.karmadeb.locklogin.neo.api.messaging.protocol.payload.Envelope;
import com.github.karmadeb.locklogin.neo.api.messaging.protocol.payload.Payload;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class NeoEnvelope<T extends Payload> extends Envelope<T> {

	private final T payload;
	private final Collection<Destination> destinations;

	@Contract("null, _ -> fail")
	public NeoEnvelope(final T payload, final Destination... destinations) {
		this.payload = Objects.requireNonNull(payload);
		this.destinations = List.of(destinations);
	}

	/**
	 * Get the envelope destinations. A clone of the message
	 * is created for each different destination
	 *
	 * @return the message destination
	 */
	@Override
	public @NotNull Collection<Destination> destinations() {
		return this.destinations;
	}

	/**
	 * Get the message payload
	 *
	 * @return the payload
	 */
	@Override
	public @NotNull T payload() {
		return this.payload;
	}
}
