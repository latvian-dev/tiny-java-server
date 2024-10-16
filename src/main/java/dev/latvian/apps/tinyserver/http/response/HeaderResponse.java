package dev.latvian.apps.tinyserver.http.response;

public record HeaderResponse(HTTPResponse original, String header, String value) implements HTTPResponse {
	@Override
	public HTTPStatus status() {
		return original.status();
	}

	@Override
	public void build(HTTPPayload payload) {
		payload.addHeader(header, value);
		original.build(payload);
	}
}
