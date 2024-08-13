package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.StatusCode;
import dev.latvian.apps.tinyserver.http.HTTPRequest;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class WSSession<REQ extends HTTPRequest> {
	WSHandler<REQ, ?> handler;
	UUID id;
	StatusCode closeReason;

	public UUID id() {
		return id;
	}

	public void send(WSPayload payload) {
		// FIXME
	}

	public void sendText(String payload) {
		send(new WSPayload(true, payload.getBytes(StandardCharsets.UTF_8)));
	}

	public void sendBinary(byte[] payload) {
		send(new WSPayload(false, payload));
	}

	public void onOpen(REQ req) {
	}

	public void onClose(StatusCode reason, boolean remote) {
	}

	public void onError(Throwable error) {
		error.printStackTrace();
	}

	public void onTextMessage(String message) {
	}

	public void onBinaryMessage(byte[] message) {
	}

	public void close(String reason) {
		closeReason = new StatusCode(1001, reason); // FIXME
	}
}
