package dev.latvian.apps.tinyserver.http.response;

import dev.latvian.apps.tinyserver.http.response.encoding.ResponseContentEncoding;

public record EncodingResponse(HTTPResponse original, ResponseContentEncoding encoding) implements HTTPResponse {
	@Override
	public HTTPStatus status() {
		return original.status();
	}

	@Override
	public void build(HTTPPayload payload) {
		payload.addEncoding(encoding);
		original.build(payload);
	}
}
