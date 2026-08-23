package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.CloseReason;
import dev.latvian.apps.tinyhttp.HTTPConnection;
import dev.latvian.apps.tinyhttp.StatusCode;
import dev.latvian.apps.tinyhttp.http.HTTPRequest;
import dev.latvian.apps.tinyhttp.http.HTTPUpgrade;
import dev.latvian.apps.tinyhttp.util.OutputOperations;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;

public class WSSession<REQ extends HTTPRequest> implements HTTPUpgrade<REQ> {
	WSEndpointHandler<REQ, ?> handler;
	String key;
	HTTPConnection<REQ> connection;
	OutputOperations outputOperations;
	WSRXThread rxThread;
	boolean closed = false;

	@Override
	public final void start(REQ req) {
		this.connection = (HTTPConnection<REQ>) req.connection();
		this.outputOperations = connection.server().getSharedOutputOperations();

		if (this.outputOperations == null) {
			this.outputOperations = new WSOutputOperations(this, "TX-WS-" + key);
		}

		this.rxThread = new WSRXThread(connection.server(), this);
		this.rxThread.setDaemon(true);
		this.rxThread.start();

		onOpen();
	}

	@Override
	public String protocol() {
		return "websocket";
	}

	@Override
	public final boolean isClosed() {
		return closed;
	}

	public final String key() {
		return key;
	}

	public final void send(Frame frame) {
		if (!closed) {
			outputOperations.queue(new QueuedFrame<>(this, frame));
		}
	}

	public final boolean sendNow(Frame frame) {
		outputOperations.lock.lock();

		try {
			return frame.write(this, outputOperations);
		} finally {
			outputOperations.lock.unlock();
		}
	}

	public void sendHeartbeat() {
		sendNow(Frame.EMPTY_PING);
	}

	public void onOpen() {
	}

	public void onClose(CloseReason reason, @Nullable Throwable error) {
	}

	@Nullable
	public Frame onTextMessage(ByteBuffer payload) {
		return onTextMessage(StandardCharsets.UTF_8.decode(payload).toString());
	}

	@Nullable
	public Frame onTextMessage(String payload) {
		return null;
	}

	@Nullable
	public Frame onBinaryMessage(ByteBuffer payload) {
		return null;
	}

	public void onPing(ByteBuffer payload) {
	}

	public void onPong(ByteBuffer payload) {
	}

	void close0(CloseReason closeReason, @Nullable Throwable error) {
		if (!closed) {
			closed = true;
			handler.removeSession(this);
			var rx = rxThread;
			rxThread = null;

			if (rx != null && rx.isAlive()) {
				try {
					rx.interrupt();
				} catch (Throwable ignore) {
				}
			}

			onClose(closeReason, error);
		}
	}

	public void handleException(Throwable ex) {
		if (ex instanceof ClosedByInterruptException || ex instanceof InterruptedException) {
			close0(new CloseReason(WSCloseStatus.CLOSED.statusCode, false), null);
		} else if (ex instanceof ClosedChannelException) {
			close0(new CloseReason(WSCloseStatus.CLOSED.statusCode, false), ex);
		} else {
			close0(new CloseReason(WSCloseStatus.INTERNAL_ERROR.statusCode, false), ex);
		}
	}

	public final void close(StatusCode statusCode) {
		if (sendNow(Frame.close(statusCode.code(), statusCode.message()))) {
			close0(new CloseReason(statusCode, false), null);
		}
	}

	public final void close(WSCloseStatus status) {
		close(status.statusCode);
	}

	public final void close(String message) {
		close(WSCloseStatus.CLOSED.statusCode.withMessage(message));
	}

	@Override
	public String toString() {
		return "WS-" + key;
	}
}
