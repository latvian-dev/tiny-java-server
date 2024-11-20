package dev.latvian.apps.tinyserver.http;

import dev.latvian.apps.tinyserver.http.response.HTTPResponse;

public interface HTTPHandler<REQ extends HTTPRequest> {
	HTTPResponse handle(REQ req) throws Exception;

	default boolean isFileHandler() {
		return false;
	}
}
