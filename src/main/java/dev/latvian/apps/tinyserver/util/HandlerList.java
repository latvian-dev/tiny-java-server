package dev.latvian.apps.tinyserver.util;

import dev.latvian.apps.tinyserver.http.HTTPMethod;
import dev.latvian.apps.tinyserver.http.HTTPRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record HandlerList<REQ extends HTTPRequest>(HTTPMethod method, Map<String, HTTPPathHandler<REQ>> staticHandlers, List<HTTPPathHandler<REQ>> dynamicHandlers) {
	public HandlerList(HTTPMethod method) {
		this(method, new HashMap<>(), new ArrayList<>());
	}
}