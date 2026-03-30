package dev.latvian.apps.tinyserver.util;

import dev.latvian.apps.tinyserver.http.HTTPHandler;
import dev.latvian.apps.tinyserver.http.HTTPMethod;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public record HTTPPathHandler<REQ extends HTTPRequest>(HTTPMethod method, CompiledPath path, HTTPHandler<REQ> handler) {
	public static final HTTPPathHandler<?> DEFAULT = new HTTPPathHandler<>(HTTPMethod.GET, CompiledPath.EMPTY, req -> HTTPStatus.OK);

	@Override
	public String toString() {
		return method + " /" + path;
	}
}
