package dev.latvian.apps.tinyhttp.http.response.encoding;

import dev.latvian.apps.tinyhttp.content.ResponseContent;

import java.io.IOException;

public interface ResponseContentEncoding {
	String name();

	ResponseContent encode(ResponseContent body) throws IOException;
}
