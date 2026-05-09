package dev.latvian.apps.tinyhttp.http;

import dev.latvian.apps.tinyhttp.http.response.HTTPResponse;

public interface HTTPHandler<REQ extends HTTPRequest> {
	HTTPResponse handle(REQ req) throws Exception;

	default boolean isFileHandler() {
		return false;
	}
}
