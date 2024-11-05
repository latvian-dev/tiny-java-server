package dev.latvian.apps.tinyserver.http.file;

import dev.latvian.apps.tinyserver.http.HTTPHandler;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record DynamicFileHandler<REQ extends HTTPRequest>(Path directory, FileResponseHandler responseHandler, boolean autoIndex) implements HTTPHandler<REQ> {
	@Override
	public HTTPResponse handle(REQ req) throws IOException {
		var pathVar = req.variables().get("path");

		if (pathVar == null) {
			if (autoIndex && Files.exists(directory) && Files.isDirectory(directory) && Files.isReadable(directory)) {
				return FileIndexHandler.index("/" + req.path(), directory, directory);
			}

			return HTTPStatus.NOT_FOUND;
		}

		var path = directory.resolve(pathVar);

		if (path.startsWith(directory) && Files.exists(path) && Files.isReadable(path)) {
			if (Files.isRegularFile(path)) {
				return responseHandler.apply(HTTPResponse.ok().content(path), false, path);
			} else if (autoIndex && Files.isDirectory(path)) {
				return responseHandler.apply(FileIndexHandler.index("/" + req.path(), directory, path), true, path);
			}
		}

		return HTTPStatus.NOT_FOUND;
	}
}
