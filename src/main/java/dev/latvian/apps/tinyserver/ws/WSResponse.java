package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPResponseBuilder;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

import java.util.Base64;

public record WSResponse(WSSession<?> session, byte[] accept) implements HTTPResponse {
	@Override
	public void build(HTTPResponseBuilder payload) {
		payload.setStatus(HTTPStatus.SWITCHING_PROTOCOLS);
		payload.setHeader("Upgrade", "websocket");
		payload.setHeader("Connection", "Upgrade");
		payload.setHeader("Sec-WebSocket-Accept", Base64.getEncoder().encodeToString(accept));
	}
}
