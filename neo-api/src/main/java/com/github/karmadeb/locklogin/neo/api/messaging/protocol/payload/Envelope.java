package com.github.karmadeb.locklogin.neo.api.messaging.protocol.payload;

import com.github.karmadeb.locklogin.neo.api.messaging.Channel;
import com.github.karmadeb.locklogin.neo.api.messaging.Destination;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * The envelope represents a message sent through
 * a {@link Channel}. The envelope must know how to
 * encode and decode itself.
 * Please note that large messages may be split
 * in order to prevent the channel from collapsing,
 * even though the "{@link Codec#decode(byte[])}" method
 * will still receive the complete payload.
 */
public abstract class Envelope<T extends Payload> implements Cloneable {

	/**
	 * Get the envelope destinations. A clone of the message
	 * is created for each different destination
	 * @return the message destination
	 */
	@NotNull
	public abstract Collection<Destination> destinations();

	/**
	 * Get the message payload
	 * @return the payload
	 */
	@NotNull
	public abstract T payload();

	/**
	 * Clone the message. This method should
	 * never throw
	 * @return the cloned message
	 */
	@NotNull
	public final Envelope<T> clone() {
		try {
			Object obj = super.clone();
			// noinspection unchecked, rawtypes
			return (Envelope) obj;
		} catch (CloneNotSupportedException _) {}

		throw new IllegalStateException("Somehow clone was not supported");
	}
}
