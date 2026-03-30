package dev.latvian.apps.tinyserver.util;

import dev.latvian.apps.tinyserver.http.HTTPMethod;

public record PathKey(HTTPMethod method, String path) {
}
