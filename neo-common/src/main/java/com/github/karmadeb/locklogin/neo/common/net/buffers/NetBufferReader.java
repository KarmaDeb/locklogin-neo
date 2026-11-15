package com.github.karmadeb.locklogin.neo.common.net.buffers;

import org.jetbrains.annotations.Contract;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import static com.github.karmadeb.locklogin.neo.common.net.buffers.NetBufferWriter.*;

/**
 * Represents a buffer of bytes reader.
 */
public final class NetBufferReader implements AutoCloseable {

	private final InputStream stream;

	/**
	 * Create a new buffer reader
	 * @param content the bytes content
	 * @throws IOException if the reader fails to create
	 */
	@Contract("null -> fail")
	public NetBufferReader(final Path content) throws IOException {
		this(Files.newInputStream(Objects.requireNonNull(content), StandardOpenOption.READ));
	}

	/**
	 * Create a new buffer reader
	 * @param stream the content
	 */
	public NetBufferReader(final InputStream stream) {
		this.stream = Objects.requireNonNull(stream);
	}

	/**
	 * Read data from the buffer
	 * @return the data
	 * @throws IOException if the data fails to read
	 */
	public byte readByte() throws IOException {
		this.checkPtr(BYTE_PTR_BYTE, "byte");

		this.stream.mark(1);
		try {
			return (byte) this.stream.read();
		} catch (IOException ex) {
			this.stream.reset();
			throw ex;
		} catch (Throwable ex) {
			this.stream.reset();
			throw new IOException(ex);
		}
	}

	/**
	 * Read data from the buffer
	 * @return the data
	 * @throws IOException if the data fails to read
	 */
	public short readShort() throws IOException {
		this.checkPtr(BYTE_PTR_SHORT, "short");

		this.stream.mark(2);
		try {
			byte[] buff = new byte[2];
			int _ = this.stream.read(buff);

			return (short) (
				(buff[0] & 0xFF) |
					((buff[1] & 0xFF) << 8)
			);
		} catch (IOException ex) {
			this.stream.reset();
			throw ex;
		} catch (Throwable ex) {
			this.stream.reset();
			throw new IOException(ex);
		}
	}

	/**
	 * Read data from the buffer
	 * @return the data
	 * @throws IOException if the data fails to read
	 */
	public int readInt() throws IOException {
		this.checkPtr(BYTE_PTR_INT, "int");

		this.stream.mark(4);
		try {
			byte[] buff = new byte[4];
			int _ = this.stream.read(buff);

			return (short) (
					(buff[0] & 0xFF) |
							((buff[1] & 0xFF) << 8) |
							((buff[2] & 0xFF) << 16) |
							((buff[3] & 0xFF) << 24)
			);
		} catch (IOException ex) {
			this.stream.reset();
			throw ex;
		} catch (Throwable ex) {
			this.stream.reset();
			throw new IOException(ex);
		}
	}

	/**
	 * Read data from the buffer
	 * @return the data
	 * @throws IOException if the data fails to read
	 */
	public long readLong() throws IOException {
		this.checkPtr(BYTE_PTR_LONG, "long");

		this.stream.mark(4);
		try {
			byte[] buff = new byte[4];
			int _ = this.stream.read(buff);

			return (short) (
					(buff[0] & 0xFF) |
							((long) (buff[1] & 0xFF) << 8) |
							((long) (buff[2] & 0xFF) << 16) |
							((long) (buff[3] & 0xFF) << 24) |
							((long) (buff[3] & 0xFF) << 32) |
							((long) (buff[3] & 0xFF) << 40) |
							((long) (buff[3] & 0xFF) << 48) |
							((long) (buff[3] & 0xFF) << 56)
			);
		} catch (IOException ex) {
			this.stream.reset();
			throw ex;
		} catch (Throwable ex) {
			this.stream.reset();
			throw new IOException(ex);
		}
	}

	/**
	 * Read data from the buffer
	 * @return the data
	 * @throws IOException if the data fails to read
	 */
	public float readFloat() throws IOException {
		this.checkPtr(BYTE_PTR_FLOAT, "float");
		int intBytes = this.readInt();

		return Float.intBitsToFloat(intBytes);
	}

	/**
	 * Read data from the buffer
	 * @return the data
	 * @throws IOException if the data fails to read
	 */
	public double readDouble() throws IOException {
		this.checkPtr(BYTE_PTR_DOUBLE, "double");
		long longBytes = this.readLong();

		return Double.longBitsToDouble(longBytes);
	}

	/**
	 * Read data from the buffer
	 * @return the data
	 * @throws IOException if the data fails to read
	 */
	public char readChar() throws IOException {
		this.checkPtr(BYTE_PTR_CHAR, "char");

		this.stream.mark(2);
		try {
			byte[] buff = new byte[2];
			int _ = this.stream.read(buff);

			return (char) (
					(buff[0] & 0xFF) |
							((buff[1] & 0xFF) << 8)
			);
		} catch (IOException ex) {
			this.stream.reset();
			throw ex;
		} catch (Throwable ex) {
			this.stream.reset();
			throw new IOException(ex);
		}
	}

	/**
	 * Read data from the buffer
	 * @return the data
	 * @throws IOException if the data fails to read
	 */
	public String readString() throws IOException {
		this.checkPtr(BYTE_PTR_TEXT, "text");

		int length = this.readInt();
		this.stream.mark(length);

		try {
			byte[] buff = new byte[length];
			int _ = this.stream.read(buff);

			int charsetLength = this.readInt();
			byte[] charsetBuff = new byte[charsetLength];

			int _ = this.stream.read(charsetBuff);
			String charsetName = new String(charsetBuff, StandardCharsets.UTF_8);

			Charset charset = Charset.forName(charsetName, StandardCharsets.UTF_8);
			return new String(buff, charset);
		} catch (IOException ex) {
			this.stream.reset();
			throw ex;
		} catch (Throwable ex) {
			this.stream.reset();
			throw new IOException(ex);
		}
	}

	/**
	 * Read data from the buffer
	 * @return the data
	 * @throws IOException if the data fails to read
	 */
	public boolean readBoolean() throws IOException {
		this.checkPtr(BYTE_PTR_BOOL, "boolean");

		this.stream.mark(1);

		try {
			byte b = (byte) this.stream.read();
			return b == 1;
		} catch (IOException ex) {
			this.stream.reset();
			throw ex;
		} catch (Throwable ex) {
			this.stream.reset();
			throw new IOException(ex);
		}
	}

	/**
	 * Read data from the buffer
	 * @return the data
	 * @throws IOException if the data fails to read
	 */
	public byte[] readBytes() throws IOException {
		this.checkPtr(BYTE_PTR_BYTES, "boolean");

		int length = this.readInt();
		this.stream.mark(length);

		try {
			byte[] buff = new byte[length];
			int _ = this.stream.read(buff);

			return buff;
		} catch (IOException ex) {
			this.stream.reset();
			throw ex;
		} catch (Throwable ex) {
			this.stream.reset();
			throw new IOException(ex);
		}
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

	private void checkPtr(final int expectedType, final String rawTypeName) throws IOException {
		this.stream.mark(4);
		try {
			byte[] buff = new byte[4];
			int _ = this.stream.read(buff);

			int ptr = (
				(buff[0] & 0xFF) |
					((buff[1] & 0xFF) << 8) |
					((buff[2] & 0xFF) << 16) |
					((buff[3] & 0xFF) << 24)
			);

			if (ptr != expectedType) {
				this.stream.reset();
				throw new IOException(String.format("Cannot read from buffer. Next datum is not a %s",
						rawTypeName));
			}
		} catch (IOException ex) {
			this.stream.reset();
			throw ex;
		} catch (Throwable ex) {
			this.stream.reset();
			throw new IOException(ex);
		}
	}
}