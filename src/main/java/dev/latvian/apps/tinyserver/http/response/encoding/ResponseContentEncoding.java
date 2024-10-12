package dev.latvian.apps.tinyserver.http.response.encoding;

import dev.latvian.apps.tinyserver.content.ResponseContent;

import java.io.IOException;

public interface ResponseContentEncoding {
	String name();

	ResponseContent encode(ResponseContent body) throws IOException;
}
