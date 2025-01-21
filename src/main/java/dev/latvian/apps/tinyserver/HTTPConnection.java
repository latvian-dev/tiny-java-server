package dev.latvian.apps.tinyserver;

import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.HTTPUpgrade;
import dev.latvian.apps.tinyserver.http.response.HTTPPayload;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class HTTPConnection<REQ extends HTTPRequest> implements Runnable {
	public static final StatusCode OPEN = new StatusCode(0, "Open");
	public static final StatusCode CLOSED = new StatusCode(1, "Closed");
	public static final StatusCode TIMEOUT = new StatusCode(2, "Timeout");
	public static final StatusCode SOCKET_CLOSED = new StatusCode(3, "Socket Closed");
	public static final StatusCode INVALID_REQUEST = new StatusCode(3, "Invalid HTTP Request");

	private final HTTPServer<REQ> server;
	private final SocketChannel socketChannel;
	public final Instant createdTime;
	long lastActivity;
	private final ByteBuffer singleByte;
	private final byte[] temp;
	HTTPUpgrade<REQ> upgrade;
	StatusCode status = OPEN;

	public HTTPConnection(HTTPServer<REQ> server, SocketChannel socketChannel, Instant createdTime) {
		this.server = server;
		this.socketChannel = socketChannel;
		this.createdTime = createdTime;
		this.singleByte = ByteBuffer.allocate(1);
		this.temp = new byte[8];
	}

	public HTTPServer<REQ> server() {
		return server;
	}

	@Nullable
	public HTTPUpgrade<REQ> upgrade() {
		return upgrade;
	}

	@Override
	public void run() {
		try {
			// noinspection StatementWithEmptyBody
			while (!socketChannel.finishConnect()) ;
			// noinspection StatementWithEmptyBody
			while (server.handleClient(this)) ;

			if (upgrade == null) {
				close();
			}
		} catch (Throwable ex) {
			error(ex);
		}
	}

	public final void close() {
		if (status == OPEN) {
			status = CLOSED;
		}
	}

	public final void close(String reason, boolean error) {
		if (status == OPEN) {
			status = new StatusCode(error ? 3 : 1, reason);
		}
	}

	final boolean handleClosure() {
		if (status == OPEN && !socketChannel.isOpen()) {
			status = SOCKET_CLOSED;
		}

		if (status == OPEN && upgrade != null && upgrade.isClosed()) {
			status = CLOSED;
		}

		if (status == OPEN && upgrade == null && server.now - lastActivity > server.keepAliveTimeout * 1000L) {
			status = TIMEOUT;
		}

		if (status == OPEN) {
			return false;
		}

		try {
			socketChannel.shutdownInput();
		} catch (IOException ex) {
			error(ex);
		}

		try {
			socketChannel.shutdownOutput();
		} catch (IOException ex) {
			error(ex);
		}

		try {
			socketChannel.close();
		} catch (IOException ex) {
			error(ex);
		}

		closed(status);
		return true;
	}

	protected void beforeHandshake() {
	}

	protected void closed(StatusCode reason) {
	}

	protected void error(Throwable error) {
		if (!(error instanceof SocketTimeoutException || error instanceof ClosedChannelException || error instanceof IOException io && "Broken pipe".equals(io.getMessage()))) {
			error.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return socketChannel.socket().getPort() + " @ " + HTTPPayload.DATE_TIME_FORMATTER.format(createdTime) + (upgrade == null ? "" : (" (" + upgrade.protocol() + ")"));
	}

	public int readDirectly(ByteBuffer buffer) throws IOException {
		return socketChannel.read(buffer);
	}

	public void read(ByteBuffer buffer) throws IOException {
		while (buffer.hasRemaining()) {
			readDirectly(buffer);
		}
	}

	public void readBytes(byte[] bytes, int off, int len) throws IOException {
		for (var i = 0; i < len; i++) {
			singleByte.clear();

			int r;

			do {
				r = readDirectly(singleByte);
			}
			while (r != 1);

			bytes[off + i] = singleByte.get(0);
		}
	}

	public void readBytes(byte[] bytes) throws IOException {
		readBytes(bytes, 0, bytes.length);
	}

	public byte readByte() throws IOException {
		readBytes(temp, 0, 1);
		return temp[0];
	}

	public short readShort() throws IOException {
		readBytes(temp, 0, 2);
		return (short) ((temp[0] & 0xFF) << 8 | (temp[1] & 0xFF));
	}

	public int readInt() throws IOException {
		readBytes(temp, 0, 4);
		return (temp[0] & 0xFF) << 24 | (temp[1] & 0xFF) << 16 | (temp[2] & 0xFF) << 8 | (temp[3] & 0xFF);
	}

	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public long readLong() throws IOException {
		readBytes(temp, 0, 8);
		return (long) (temp[0] & 0xFF) << 56 | (long) (temp[1] & 0xFF) << 48 | (long) (temp[2] & 0xFF) << 40 | (long) (temp[3] & 0xFF) << 32 | (long) (temp[4] & 0xFF) << 24 | (long) (temp[5] & 0xFF) << 16 | (long) (temp[6] & 0xFF) << 8 | (long) (temp[7] & 0xFF);
	}

	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	public String readCRLF() throws IOException {
		var bytes = new ByteArrayOutputStream();

		while (true) {
			int b = readByte();

			if (b == '\r') {
				var r = readByte();

				if (r == '\n') {
					break;
				} else {
					bytes.write('\r');
					bytes.write(r);
				}
			} else {
				bytes.write(b);
			}
		}

		return bytes.toString(StandardCharsets.UTF_8);
	}

	public void writeDirectly(ByteBuffer buffer) throws IOException {
		socketChannel.write(buffer);
	}

	public void write(ByteBuffer buffer) throws IOException {
		while (buffer.hasRemaining()) {
			writeDirectly(buffer);
		}
	}
}
