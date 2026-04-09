package dev.latvian.apps.tinyserver.http.tus;

import java.util.Map;

public record TUSCreationData(String location, Map<String, ?> headers) {
	public TUSCreationData(String location) {
		this(location, Map.of());
	}
}