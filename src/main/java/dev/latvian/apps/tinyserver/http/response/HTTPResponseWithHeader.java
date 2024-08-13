package dev.latvian.apps.tinyserver.http.response;

public record HTTPResponseWithHeader(HTTPResponse original, String header, String value) implements HTTPResponse {
	@Override
	public void build(HTTPResponseBuilder payload) throws Exception {
		payload.setHeader(header, value);
		original.build(payload);
	}
}
