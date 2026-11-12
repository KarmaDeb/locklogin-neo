package com.github.karmadeb.locklogin.neo.api.messaging.protocol.payload;

import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;

/**
 * Represents a message payload
 */
public abstract class Payload {

	private final NamespacedIdentity id;

	/**
	 * Create a new payload
	 * @param id the ID of the payload type
	 */
	public Payload(final NamespacedIdentity id) {
		this.id = id;
	}

	/**
	 * Get the payload ID
	 * @return the ID
	 */
	public final NamespacedIdentity id() {
		return this.id;
	}
}
