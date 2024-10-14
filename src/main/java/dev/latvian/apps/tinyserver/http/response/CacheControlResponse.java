package dev.latvian.apps.tinyserver.http.response;

import java.time.Duration;

public record CacheControlResponse(HTTPResponse original, String value) implements HTTPResponse {
	public CacheControlResponse(HTTPResponse original, boolean isPublic, Duration duration) {
		this(original, duration.isPositive() ? ((isPublic ? "public, max-age=" : "private, max-age=") + duration.toSeconds()) : "no-cache, no-store, must-revalidate, max-age=0");
	}

	@Override
	public HTTPStatus status() {
		return original.status();
	}

	@Override
	public void build(HTTPPayload payload) {
		payload.setCacheControl(value);
		original.build(payload);
	}
}
