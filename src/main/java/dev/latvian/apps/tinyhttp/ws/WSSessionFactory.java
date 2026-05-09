package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.http.HTTPRequest;

@FunctionalInterface
public interface WSSessionFactory<REQ extends HTTPRequest, WSS extends WSSession<REQ>> {
	WSS create(REQ req) throws Exception;
}
