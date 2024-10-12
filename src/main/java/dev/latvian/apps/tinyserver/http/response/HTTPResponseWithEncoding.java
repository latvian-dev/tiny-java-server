package dev.latvian.apps.tinyserver.http.response;

import dev.latvian.apps.tinyserver.http.response.encoding.ResponseContentEncoding;

public record HTTPResponseWithEncoding(HTTPResponse original, ResponseContentEncoding encoding) implements HTTPResponse {
	@Override
	public void build(HTTPPayload payload) throws Exception {
		payload.addEncoding(encoding);
		original.build(payload);
	}
}
