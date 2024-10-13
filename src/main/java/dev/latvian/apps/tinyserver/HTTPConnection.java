package dev.latvian.apps.tinyserver;

import dev.latvian.apps.tinyserver.http.response.HTTPPayload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class HTTPConnection implements AutoCloseable, Runnable {
	public final HTTPServer<?> server;
	public final SocketChannel socketChannel;
	public final Instant createdTime;
	long lastActivity;
	private final ByteBuffer singleByte;

	public HTTPConnection(HTTPServer<?> server, SocketChannel socketChannel, Instant createdTime) {
		this.server = server;
		this.socketChannel = socketChannel;
		this.createdTime = createdTime;
		this.singleByte = ByteBuffer.allocate(1);
	}

	@Override
	public void run() {
		try {
			server.handleClient(this);
		} catch (Throwable ex) {
			error(ex);
		}
	}

	@Override
	public void close() {
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

		closed();
	}

	public boolean isClosed() {
		return !socketChannel.isOpen();
	}

	protected void beforeHandshake() {
	}

	protected void closed() {
	}

	protected void error(Throwable error) {
		if (!(error instanceof SocketTimeoutException || error instanceof ClosedChannelException)) {
			error.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return socketChannel.socket().getPort() + " @ " + HTTPPayload.DATE_TIME_FORMATTER.format(createdTime);
	}

	public void readBytes(byte[] bytes) throws IOException {
		var buf = ByteBuffer.wrap(bytes);
		socketChannel.read(buf);
		buf.flip();
		buf.get(bytes);
	}

	public byte readByte() throws IOException {
		singleByte.clear();
		socketChannel.read(singleByte);
		singleByte.flip();
		return singleByte.get();
	}

	public short readShort() throws IOException {
		var buf = ByteBuffer.allocate(2);
		socketChannel.read(buf);
		buf.flip();
		return buf.getShort();
	}

	public int readInt() throws IOException {
		var buf = ByteBuffer.allocate(4);
		socketChannel.read(buf);
		buf.flip();
		return buf.getInt();
	}

	public long readLong() throws IOException {
		var buf = ByteBuffer.allocate(8);
		socketChannel.read(buf);
		buf.flip();
		return buf.getLong();
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
}
