package dev.latvian.apps.tinyserver.http;

import dev.latvian.apps.tinyserver.http.response.HTTPPayload;

public interface HTTPOptionsHandler<REQ extends HTTPRequest> {
	default void handle(REQ req, HTTPPayload payload) throws Exception {
		addHeaders(req, payload);
	}

	void addHeaders(REQ req, HeaderConsumer headers) throws Exception;
}
