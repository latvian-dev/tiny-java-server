package dev.latvian.apps.tinyserver.util;

import dev.latvian.apps.tinyserver.http.HTTPOptionsHandler;
import dev.latvian.apps.tinyserver.http.HTTPRequest;

public record HTTPOptionsPathHandler<REQ extends HTTPRequest>(CompiledPath path, HTTPOptionsHandler<REQ> handler) {
	@Override
	public String toString() {
		return path.toString();
	}
}
