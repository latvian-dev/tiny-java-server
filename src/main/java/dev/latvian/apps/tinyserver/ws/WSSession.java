package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.StatusCode;
import dev.latvian.apps.tinyserver.http.HTTPRequest;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;

public class WSSession<REQ extends HTTPRequest> {
	Map<UUID, WSSession<?>> sessionMap;
	UUID id;
	TXThread txThread;
	RXThread rxThread;

	public final void start(Socket socket, InputStream in, OutputStream out) {
		this.txThread = new TXThread(this, socket, in, out);
		this.txThread.setDaemon(true);

		this.rxThread = new RXThread(this);
		this.rxThread.setDaemon(true);

		this.txThread.start();
		this.rxThread.start();
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

	public final void close(WSCloseStatus status, String reason) {
		txThread.remoteClosed = false;
		txThread.closeReason = new StatusCode(status.statusCode.code(), reason);
		LockSupport.unpark(txThread);
	}
}
