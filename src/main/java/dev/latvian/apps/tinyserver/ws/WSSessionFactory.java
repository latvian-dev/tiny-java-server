package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.http.HTTPRequest;

@FunctionalInterface
public interface WSSessionFactory<REQ extends HTTPRequest, WSS extends WSSession<REQ>> {
	WSSessionFactory<HTTPRequest, WSSession<HTTPRequest>> DEFAULT = WSSession::new;

	WSS create();
}
