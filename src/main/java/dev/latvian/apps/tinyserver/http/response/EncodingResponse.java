package dev.latvian.apps.tinyserver.http.response;

import dev.latvian.apps.tinyserver.http.response.encoding.ResponseContentEncoding;

public record EncodingResponse(HTTPResponse original, ResponseContentEncoding encoding) implements ChainedHTTPResponse {
	@Override
	public void build(HTTPPayload payload) {
		payload.addEncoding(encoding);
		original.build(payload);
	}
}
