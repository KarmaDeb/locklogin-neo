package com.github.karmadeb.locklogin.neo.api.messaging.protocol.payload;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public interface Codec<T extends Payload> {

	/**
	 * Encode a payload
	 * @param payload the payload to encode
	 * @return the encoded payload
	 * @throws IOException if there's any problem while encoding
	 */
	@NotNull InputStream encode(final @NotNull T payload) throws IOException;

	/**
	 * Decode a payload
	 * @param payload the payload to decode
	 * @return the decoded payload
	 * @throws IOException if there's any problem while decoding
	 */
	@NotNull
	Optional<T> decode(final @NotNull InputStream payload) throws IOException;
}
