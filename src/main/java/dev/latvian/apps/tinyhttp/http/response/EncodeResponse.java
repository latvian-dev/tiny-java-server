package dev.latvian.apps.tinyhttp.http.response;

public record EncodeResponse(HTTPResponse original, String encode) implements ChainedHTTPResponse {
	@Override
	public void build(HTTPPayload payload) {
		payload.setEncode(encode);
		original.build(payload);
	}
}
