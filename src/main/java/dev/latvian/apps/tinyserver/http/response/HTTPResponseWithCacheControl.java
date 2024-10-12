package dev.latvian.apps.tinyserver.http.response;

public record HTTPResponseWithCacheControl(HTTPResponse original, String value) implements HTTPResponse {
	public HTTPResponseWithCacheControl(HTTPResponse original, boolean isPublic, int seconds) {
		this(original, seconds <= 0 ? "no-cache, no-store, must-revalidate, max-age=0" : ((isPublic ? "public, max-age=" : "private, max-age=") + seconds));
	}

	@Override
	public void build(HTTPPayload payload) throws Exception {
		payload.setCacheControl(value);
		original.build(payload);
	}
}
