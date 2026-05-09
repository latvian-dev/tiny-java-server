package dev.latvian.apps.tinyhttp.util;

import dev.latvian.apps.tinyhttp.http.HTTPOptionsHandler;
import dev.latvian.apps.tinyhttp.http.HTTPRequest;

public record HTTPOptionsPathHandler<REQ extends HTTPRequest>(CompiledPath path, HTTPOptionsHandler<REQ> handler) {
	@Override
	public String toString() {
		return path.toString();
	}
}
