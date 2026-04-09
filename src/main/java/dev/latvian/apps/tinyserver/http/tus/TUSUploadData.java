package dev.latvian.apps.tinyserver.http.tus;

import java.util.Map;

public record TUSUploadData(long offset, Map<String, ?> headers) {
	public TUSUploadData(long offset) {
		this(offset, Map.of());
	}
}