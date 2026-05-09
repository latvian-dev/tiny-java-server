package dev.latvian.apps.tinyhttp.util;

import dev.latvian.apps.tinyhttp.http.HTTPHandler;
import dev.latvian.apps.tinyhttp.http.HTTPMethod;
import dev.latvian.apps.tinyhttp.http.HTTPRequest;

public record HTTPPathHandler<REQ extends HTTPRequest>(HTTPMethod method, CompiledPath path, HTTPHandler<REQ> handler) {
	@Override
	public String toString() {
		return method + " /" + path;
	}
}
