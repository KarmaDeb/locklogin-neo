package com.github.karmadeb.locklogin.neo.common.network.messaging;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.messaging.protocol.Protocol;
import com.github.karmadeb.locklogin.neo.api.messaging.protocol.payload.Codec;
import com.github.karmadeb.locklogin.neo.common.network.buffers.NetBufferReader;
import com.github.karmadeb.locklogin.neo.common.network.buffers.NetBufferWriter;
import com.github.karmadeb.locklogin.neo.common.network.messaging.payload.NeoPayload;
import com.github.karmadeb.locklogin.neo.common.network.messaging.payload.UserAuthPayload;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@SuppressWarnings("unused")
public class NeoCodec implements Codec<NeoPayload> {

	/**
	 * Encode a payload
	 *
	 * @param payload the payload to encode
	 * @return the encoded payload
	 * @throws IOException if there's any problem while encoding
	 */
	@Override
	public @NotNull InputStream encode(final @NotNull NeoPayload payload) throws IOException {
		try (NetBufferWriter writer = new NetBufferWriter()) {
			writer.write(payload.id().toString());
			payload.write(writer);

			return writer.flush();
		}
	}

	/**
	 * Decode a payload
	 *
	 * @param payload the payload to decode
	 * @return the decoded payload
	 * @throws IOException if there's any problem while decoding
	 */
	@Override
	public @NotNull Optional<NeoPayload> decode(final @NotNull InputStream payload) throws IOException {
		try (NetBufferReader reader = new NetBufferReader(payload)) {
			String rawId = reader.readString();
			NamespacedIdentity identity = NamespacedIdentity.parse(rawId);

			return PayloadRegistry.parse(identity, reader);
		}
	}

	public static void install() throws Exception {
		NeoCodec codec = new NeoCodec();
		Protocol.register(UserAuthPayload.ID, codec);
	}
}
