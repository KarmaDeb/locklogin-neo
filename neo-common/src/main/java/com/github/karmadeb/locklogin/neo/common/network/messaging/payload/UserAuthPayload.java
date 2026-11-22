package com.github.karmadeb.locklogin.neo.common.network.messaging.payload;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.common.network.buffers.NetBufferReader;
import com.github.karmadeb.locklogin.neo.common.network.buffers.NetBufferWriter;
import com.github.karmadeb.locklogin.neo.common.network.messaging.PayloadRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("unused")
public class UserAuthPayload extends NeoPayload {

	public static final NamespacedIdentity ID = NamespacedIdentity.locklogin("auth");

	static {
		PayloadRegistry.register(ID, UserAuthPayload::new);
	}

	private final UUID client;

	/**
	 * Create a new payload
	 */
	@Contract("null -> fail")
	public UserAuthPayload(final UUID clientId) {
		super(ID);
		this.client = Objects.requireNonNull(clientId);
	}

	private UserAuthPayload(final NetBufferReader reader) throws Exception {
		long least = reader.readLong();
		long most = reader.readLong();

		UUID id = new UUID(least, most);
		this(id);
	}

	public @NotNull UUID client() {
		return this.client;
	}

	/**
	 * Write the payload to the output stream
	 *
	 * @param out the stream
	 * @throws IOException if the write operation fails
	 */
	@Override
	public void write(final @NotNull NetBufferWriter out) throws IOException {
		long least = this.client.getLeastSignificantBits();
		long most = this.client.getMostSignificantBits();

		out.write(least);
		out.write(most);
	}
}
