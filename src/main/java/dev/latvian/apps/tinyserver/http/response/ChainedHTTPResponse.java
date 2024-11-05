package dev.latvian.apps.tinyserver.http.response;

public interface ChainedHTTPResponse extends HTTPResponse {
	HTTPResponse original();

	@Override
	default HTTPStatus status() {
		return original().status();
	}
}
