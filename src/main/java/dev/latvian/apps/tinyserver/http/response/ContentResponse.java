package dev.latvian.apps.tinyserver.http.response;

import dev.latvian.apps.tinyserver.content.ResponseContent;

public record ContentResponse(HTTPResponse original, ResponseContent body) implements HTTPResponse {
	@Override
	public HTTPStatus status() {
		return original.status();
	}

	@Override
	public void build(HTTPPayload payload) {
		original.build(payload);
		payload.setBody(body);
	}
}
