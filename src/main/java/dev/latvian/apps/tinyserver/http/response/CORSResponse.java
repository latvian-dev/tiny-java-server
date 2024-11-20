package dev.latvian.apps.tinyserver.http.response;

public record CORSResponse(HTTPResponse original, String value) implements ChainedHTTPResponse {
	@Override
	public void build(HTTPPayload payload) {
		payload.setCors(value);
		original.build(payload);
	}
}
