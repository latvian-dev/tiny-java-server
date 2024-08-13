package dev.latvian.apps.tinyserver.http;

import dev.latvian.apps.tinyserver.CompiledPath;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public record HTTPPathHandler<REQ extends HTTPRequest>(HTTPMethod method, CompiledPath path, HTTPHandler<REQ> handler) {
	public static final HTTPPathHandler<?> DEFAULT = new HTTPPathHandler<>(HTTPMethod.GET, CompiledPath.EMPTY, req -> HTTPStatus.OK);
}
