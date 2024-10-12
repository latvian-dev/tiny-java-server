package dev.latvian.apps.tinyserver.http;

import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record RootPathFileHandler<REQ extends HTTPRequest>(String httpPath, Path directory) implements HTTPHandler<REQ> {
	@Override
	public HTTPResponse handle(REQ req) throws IOException {
		if (Files.exists(directory) && Files.isDirectory(directory) && Files.isReadable(directory)) {
			return PathFileHandler.index(httpPath, directory, directory);
		}

		return HTTPStatus.NOT_FOUND;
	}
}
