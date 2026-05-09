package dev.latvian.apps.tinyhttp.http.response;

import dev.latvian.apps.tinyhttp.content.ResponseContent;

public record ContentResponse(HTTPResponse original, ResponseContent body) implements ChainedHTTPResponse {
	@Override
	public void build(HTTPPayload payload) {
		original.build(payload);
		payload.setBody(body);
	}
}
