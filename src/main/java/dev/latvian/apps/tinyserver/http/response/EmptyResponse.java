package dev.latvian.apps.tinyserver.http.response;

public class EmptyResponse implements HTTPResponse {
	public static final EmptyResponse INSTANCE = new EmptyResponse();

	@Override
	public void build(HTTPPayload payload) {
	}
}
