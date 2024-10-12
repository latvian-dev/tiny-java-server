package dev.latvian.apps.tinyserver.http.response;

public record RedirectResponse(HTTPStatus status, String location) implements HTTPResponse {
	@Override
	public HTTPStatus status() {
		return status;
	}

	@Override
	public void build(HTTPPayload payload) {
		payload.setHeader("Location", location);
	}
}
