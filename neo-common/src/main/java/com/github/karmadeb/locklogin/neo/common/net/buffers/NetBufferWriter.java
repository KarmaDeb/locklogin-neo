package com.github.karmadeb.locklogin.neo.common.net.buffers;

import org.jetbrains.annotations.Contract;

import java.io.*;
import java.lang.ref.Cleaner;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Represents a buffer of bytes writer
 */
public final class NetBufferWriter implements AutoCloseable {

	public static final int BYTE_PTR_BYTE = 1 << 1;
	public static final int BYTE_PTR_SHORT = 1 << 2;
	public static final int BYTE_PTR_INT = 1 << 3;
	public static final int BYTE_PTR_LONG = 1 << 4;
	public static final int BYTE_PTR_FLOAT = 1 << 5;
	public static final int BYTE_PTR_DOUBLE = 1 << 6;
	public static final int BYTE_PTR_CHAR = 1 << 7;
	public static final int BYTE_PTR_TEXT = 1 << 8;
	public static final int BYTE_PTR_BOOL = 1 << 9;
	public static final int BYTE_PTR_BYTES = 1 << 10;

	private final Path tmp;
	private final OutputStream stream;

	/**
	 * Create a new buffer writer
	 * @throws IOException if the writer fails to create
	 */
	public NetBufferWriter() throws IOException {
		this.tmp = Files.createTempFile("netbuff", "writer");
		this.tmp.toFile().deleteOnExit();

		this.stream = Files.newOutputStream(tmp, StandardOpenOption.CREATE,
				StandardOpenOption.WRITE);
	}

	/**
	 * Write data to the buffer
	 * @param data the data to write
	 * @return the buffer writer
	 * @throws IOException if the data fails to write
	 */
	public NetBufferWriter write(final byte data) throws IOException {
		this.appendPtr(BYTE_PTR_BYTE);
		this.stream.write((byte) data);
		return this;
	}

	/**
	 * Write data to the buffer
	 * @param data the data to write
	 * @return the buffer writer
	 * @throws IOException if the data fails to write
	 */
	public NetBufferWriter write(final short data) throws IOException {
		this.appendPtr(BYTE_PTR_SHORT);
		this.stream.write(new byte[]{
				(byte) data,
				(byte) (data >> 8)
		});

		return this;
	}

	/**
	 * Write data to the buffer
	 * @param data the data to write
	 * @return the buffer writer
	 * @throws IOException if the data fails to write
	 */
	public NetBufferWriter write(final int data) throws IOException {
		this.appendPtr(BYTE_PTR_INT);
		this.stream.write(new byte[]{
				(byte) data,
				(byte) (data >> 8),
				(byte) (data >> 16),
				(byte) (data >> 24)
		});

		return this;
	}

	/**
	 * Write data to the buffer
	 * @param data the data to write
	 * @return the buffer writer
	 * @throws IOException if the data fails to write
	 */
	public NetBufferWriter write(final long data) throws IOException {
		this.appendPtr(BYTE_PTR_LONG);
		this.stream.write(new byte[]{
				(byte) data,
				(byte) (data >> 8),
				(byte) (data >> 16),
				(byte) (data >> 24),
				(byte) (data >> 32),
				(byte) (data >> 40),
				(byte) (data >> 48),
				(byte) (data >> 56)
		});

		return this;
	}

	/**
	 * Write data to the buffer
	 * @param data the data to write
	 * @return the buffer writer
	 * @throws IOException if the data fails to write
	 */
	public NetBufferWriter write(final float data) throws IOException {
		this.appendPtr(BYTE_PTR_FLOAT);
		int intValue = Float.floatToIntBits(data);
		this.write(intValue);

		return this;
	}

	/**
	 * Write data to the buffer
	 * @param data the data to write
	 * @return the buffer writer
	 * @throws IOException if the data fails to write
	 */
	public NetBufferWriter write(final double data) throws IOException {
		this.appendPtr(BYTE_PTR_DOUBLE);
		long longValue = Double.doubleToLongBits(data);
		this.write(longValue);

		return this;
	}

	/**
	 * Write data to the buffer
	 * @param character the data to write
	 * @return the buffer writer
	 * @throws IOException if the data fails to write
	 */
	public NetBufferWriter write(final char character) throws IOException {
		this.appendPtr(BYTE_PTR_CHAR);

		this.stream.write(new byte[]{
				(byte) character,
				(byte) (character >> 8)
		});

		return this;
	}

	/**
	 * Write data to the buffer
	 * @param utf the data to write
	 * @return the buffer writer
	 * @throws IOException if the data fails to write
	 */
	public NetBufferWriter write(final String utf) throws IOException {
		return this.write(utf, StandardCharsets.UTF_8);
	}

	/**
	 * Write data to the buffer
	 * @param text the data to write
	 * @param charset the data charset
	 * @return the buffer writer
	 * @throws IOException if the data fails to write
	 */
	public NetBufferWriter write(final String text, final Charset charset) throws IOException {
		byte[] bytes = text.getBytes(charset);

		this.appendPtr(BYTE_PTR_TEXT);
		this.write(bytes.length);
		this.stream.write(bytes);

		String name = charset.name();
		byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
		this.write(nameBytes.length);
		this.stream.write(nameBytes);

		return this;
	}

	/**
	 * Write data to the buffer
	 * @param data the data to write
	 * @return the buffer writer
	 * @throws IOException if the data fails to write
	 */
	public NetBufferWriter write(final boolean data) throws IOException {
		this.appendPtr(BYTE_PTR_BOOL);
		this.stream.write((byte) (data ? 1 : 0));

		return this;
	}

	/**
	 * Write data to the buffer
	 * @param buff the data to write
	 * @return the buffer writer
	 * @throws IOException if the data fails to write
	 */
	@Contract("null -> fail")
	public NetBufferWriter write(final byte[] buff) throws IOException {
		Objects.requireNonNull(buff);

		this.appendPtr(BYTE_PTR_BYTES);

		byte[] clone = buff.clone();
		this.write(clone.length);
		this.write(clone);

		return this;
	}

	/**
	 * Creates a new flush input stream
	 * @return the flush stream
	 * @throws IOException if the stream fails to create
	 */
	public InputStream flush() throws IOException {
		this.stream.flush();

		return new FileInputStream(this.tmp.toFile()){
			/**
			 * Closes this file input stream and releases any system resources
			 * associated with the stream.
			 *
			 * <p> If this stream has an associated channel then the channel is closed
			 * as well.
			 *
			 * @throws IOException {@inheritDoc}
			 * @apiNote Overriding {@link #close} to perform cleanup actions is reliable
			 * only when called directly or when called by try-with-resources.
			 * @implSpec Subclasses requiring that resource cleanup take place after a stream becomes
			 * unreachable should use the {@link Cleaner} mechanism.
			 *
			 * <p>
			 * If this stream has an associated channel then this method will close the
			 * channel, which in turn will close this stream. Subclasses that override
			 * this method should be prepared to handle possible reentrant invocation.
			 */
			@Override
			public void close() throws IOException {
				super.close();

				Files.delete(NetBufferWriter.this.tmp);
			}
		};
	}

	/**
	 * Closes this resource, relinquishing any underlying resources.
	 * This method is invoked automatically on objects managed by the
	 * {@code try}-with-resources statement.
	 *
	 * @throws Exception if this resource cannot be closed
	 * @apiNote While this interface method is declared to throw {@code
	 * Exception}, implementers are <em>strongly</em> encouraged to
	 * declare concrete implementations of the {@code close} method to
	 * throw more specific exceptions, or to throw no exception at all
	 * if the close operation cannot fail.
	 *
	 * <p> Cases where the close operation may fail require careful
	 * attention by implementers. It is strongly advised to relinquish
	 * the underlying resources and to internally <em>mark</em> the
	 * resource as closed, prior to throwing the exception. The {@code
	 * close} method is unlikely to be invoked more than once and so
	 * this ensures that the resources are released in a timely manner.
	 * Furthermore it reduces problems that could arise when the resource
	 * wraps, or is wrapped, by another resource.
	 *
	 * <p><em>Implementers of this interface are also strongly advised
	 * to not have the {@code close} method throw {@link
	 * InterruptedException}.</em>
	 * <p>
	 * This exception interacts with a thread's interrupted status,
	 * and runtime misbehavior is likely to occur if an {@code
	 * InterruptedException} is {@linkplain Throwable#addSuppressed
	 * suppressed}.
	 * <p>
	 * More generally, if it would cause problems for an
	 * exception to be suppressed, the {@code AutoCloseable.close}
	 * method should not throw it.
	 *
	 * <p>Note that unlike the {@link Closeable#close close}
	 * method of {@link Closeable}, this {@code close} method
	 * is <em>not</em> required to be idempotent.  In other words,
	 * calling this {@code close} method more than once may have some
	 * visible side effect, unlike {@code Closeable.close} which is
	 * required to have no effect if called more than once.
	 * <p>
	 * However, implementers of this interface are strongly encouraged
	 * to make their {@code close} methods idempotent.
	 */
	@Override
	public void close() throws IOException {
		this.stream.close();
	}

	private void appendPtr(final int ptr) throws IOException {
		this.stream.write(new byte[]{
				(byte) ptr,
				(byte) (ptr >> 8),
				(byte) (ptr >> 16),
				(byte) (ptr >> 24),
		});
	}
}
