package dev.latvian.apps.tinyhttp;

import dev.latvian.apps.tinyhttp.http.HTTPRequest;
import dev.latvian.apps.tinyhttp.http.HTTPUpgrade;
import dev.latvian.apps.tinyhttp.http.response.HTTPPayload;
import dev.latvian.apps.tinyhttp.util.ByteChannelConnection;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.time.Instant;

public class HTTPConnection<REQ extends HTTPRequest> extends ByteChannelConnection implements Runnable {
	public static final StatusCode OPEN = new StatusCode(0, "Open");
	public static final StatusCode CLOSED = new StatusCode(1, "Closed");
	public static final StatusCode TIMEOUT = new StatusCode(2, "Timeout");
	public static final StatusCode SOCKET_CLOSED = new StatusCode(3, "Socket Closed");
	public static final StatusCode INVALID_REQUEST = new StatusCode(3, "Invalid HTTP Request");

	private final HTTPServer<REQ> server;
	private final SocketChannel socketChannel;
	public final Instant createdTime;
	long lastActivity;
	HTTPUpgrade<REQ> upgrade;
	StatusCode status = OPEN;

	public HTTPConnection(HTTPServer<REQ> server, SocketChannel socketChannel, Instant createdTime) {
		super(socketChannel);
		this.server = server;
		this.socketChannel = socketChannel;
		this.createdTime = createdTime;
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
}
