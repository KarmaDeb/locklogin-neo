package com.github.karmadeb.locklogin.neo.api.messaging;

import java.util.UUID;

/**
 * A destination represents a point in a channel. Either a
 * proxy server or a server
 */
public interface Destination {

	/**
	 * Get the destination id. The UUID is generated
	 * based on configured server address and its port
	 * number
	 * @return the ID of the server
	 */
	UUID id();

	/**
	 * Get if the destination is currently reachable.
	 * On default implementation, this is only true when
	 * the destination has at least one player connected
	 * to it
	 * @return if the destination is reachable
	 */
	boolean reachable();
}
