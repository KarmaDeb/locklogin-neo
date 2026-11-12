package com.github.karmadeb.locklogin.neo.api.messaging;

import com.github.karmadeb.locklogin.neo.api.messaging.protocol.payload.Envelope;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * A channel represents a connection between all the
 * servers. By default, LockLogin uses the communication
 * channel provided by the proxies, but other implementations
 * may choose to create their own messaging channel.
 */
public interface Channel {

	/**
	 * Get the amount of envelopes in the
	 * channel
	 * @return the amount of envelopes
	 */
    int length();

	/**
	 * Enqueue the envelope to the channel. Unlike {@link #add(Envelope)}
	 * which tries to do both (tries to send the envelope but
	 * if the channel is full, puts in the queue), this method
	 * puts directly the message into the queue and waits for
	 * the next queue processing tick to send it if possible
	 * @param envelope the message to send
	 * @return a promise which is completed always successfully after
	 * the message has been sent
	 */
	CompletableFuture<Void> enqueue(final @NotNull Envelope envelope);

	/**
	 * Add an envelope to the channel. This does not
	 * necessarily mean the message is instantly added
	 * to the channel, but that either is, or has been
	 * added to the message queue
	 * @param envelope the message to add
	 */
	void add(final @NotNull Envelope envelope);

	/**
	 * Remove an envelope from the channel. This operation
	 * succeeds only if the message is currently in the
	 * queue but has not been sent.
	 * @param envelope the message to remove
	 * @return if the message was removed
	 */
	boolean remove(final @NotNull Envelope envelope);
}
