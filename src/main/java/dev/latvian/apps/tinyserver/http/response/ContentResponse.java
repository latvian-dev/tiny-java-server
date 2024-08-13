package dev.latvian.apps.tinyserver.http.response;

import dev.latvian.apps.tinyserver.content.ResponseContent;

public record ContentResponse(HTTPResponse original, ResponseContent body) implements HTTPResponse {
	@Override
	public void build(HTTPResponseBuilder payload) throws Exception {
		original.build(payload);
		payload.setBody(body);
	}
}
