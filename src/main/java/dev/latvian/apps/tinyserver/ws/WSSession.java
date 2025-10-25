package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.HTTPConnection;
import dev.latvian.apps.tinyserver.StatusCode;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.HTTPUpgrade;

import java.util.UUID;
import java.util.concurrent.locks.LockSupport;

public class WSSession<REQ extends HTTPRequest> implements HTTPUpgrade<REQ> {
	HTTPConnection<?> connection;
	WSEndpointHandler<REQ, ?> handler;
	UUID id;
	TXThread txThread;
	RXThread rxThread;

	@Override
	public final void start(REQ req) {
		this.connection = req.connection();

		this.txThread = new TXThread(this);
		this.txThread.setDaemon(true);

		this.rxThread = new RXThread(this);
		this.rxThread.setDaemon(true);

		this.txThread.start();
		this.rxThread.start();

		onOpen(req);
	}

	@Override
	public String protocol() {
		return "websocket";
	}

	@Override
	public final boolean isClosed() {
		return txThread != null && txThread.closeReason != null;
	}

	public final UUID id() {
		return id;
	}

	public final void send(Frame frame) {
		txThread.queue.add(frame);
		LockSupport.unpark(txThread);
	}

	public final void sendText(String payload) {
		send(Frame.text(payload));
	}

	public final void sendBinary(byte[] payload) {
		send(Frame.binary(payload));
	}

	public final void sendPing(byte[] payload) {
		send(Frame.ping(payload));
	}

	public void onOpen(REQ req) {
	}

	public void onClose(StatusCode reason, boolean remote) {
	}

	public void onError(Throwable error) {
	}

	public void onTextMessage(String message) {
	}

	public void onBinaryMessage(byte[] message) {
	}

	public void onPing(byte[] payload) {
	}

	public void onPong(byte[] payload) {
	}

	public final void close(WSCloseStatus status, String reason) {
		txThread.remoteClosed = false;
		txThread.closeReason = new StatusCode(status.statusCode.code(), reason);
		LockSupport.unpark(txThread);
	}
}
