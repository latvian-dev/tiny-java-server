package dev.latvian.apps.tinyhttp.util;

import dev.latvian.apps.tinyhttp.http.HTTPMethod;

public record PathKey(HTTPMethod method, String path) {
}
