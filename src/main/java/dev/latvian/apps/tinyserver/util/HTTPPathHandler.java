package dev.latvian.apps.tinyserver.util;

import dev.latvian.apps.tinyserver.http.HTTPHandler;
import dev.latvian.apps.tinyserver.http.HTTPMethod;
import dev.latvian.apps.tinyserver.http.HTTPRequest;

public record HTTPPathHandler<REQ extends HTTPRequest>(HTTPMethod method, CompiledPath path, HTTPHandler<REQ> handler) {
	@Override
	public String toString() {
		return method + " /" + path;
	}
}
