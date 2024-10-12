package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.http.response.HTTPPayload;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

import java.util.Base64;

public record WSResponse(WSSession<?> session, byte[] accept) implements HTTPResponse {
	@Override
	public HTTPStatus status() {
		return HTTPStatus.SWITCHING_PROTOCOLS;
	}

	@Override
	public void build(HTTPPayload payload) {
		payload.addHeader("Upgrade", "websocket");
		payload.addHeader("Connection", "Upgrade");
		payload.addHeader("Sec-WebSocket-Accept", Base64.getEncoder().encodeToString(accept));
	}
}
