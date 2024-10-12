package dev.latvian.apps.tinyserver.http.response;

public record RedirectResponse(HTTPResponse original, HTTPStatus status, String location) implements HTTPResponse {
	@Override
	public void build(HTTPPayload payload) throws Exception {
		original.build(payload);
		payload.setStatus(status);
		payload.setHeader("Location", location);
	}
}
