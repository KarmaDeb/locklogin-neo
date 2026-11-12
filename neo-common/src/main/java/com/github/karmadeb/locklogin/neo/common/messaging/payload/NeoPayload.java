package com.github.karmadeb.locklogin.neo.common.messaging.payload;

import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.messaging.protocol.payload.Payload;
import com.github.karmadeb.locklogin.neo.common.buffers.NetBufferWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class NeoPayload extends Payload {

	/**
	 * Create a new payload
	 *
	 * @param id the ID of the payload type
	 */
	@Contract("null -> fail")
	public NeoPayload(final NamespacedIdentity id) {
		super(id);
	}

	/**
	 * Write the payload to the output stream
	 * @param out the stream
	 * @throws IOException if the write operation fails
	 */
	public abstract void write(final @NotNull NetBufferWriter out) throws IOException;
}
