package dev.latvian.apps.tinyserver.http;

import dev.latvian.apps.tinyserver.content.MimeType;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public record PathFileHandler<REQ extends HTTPRequest>(String httpPath, Path directory, Duration cacheDuration, boolean autoIndex) implements HTTPHandler<REQ> {
	@Override
	public HTTPResponse handle(REQ req) throws IOException {
		var pathVar = req.variables().get("path");

		if (pathVar == null) {
			if (autoIndex && Files.exists(directory) && Files.isDirectory(directory) && Files.isReadable(directory)) {
				return PathFileHandler.index(httpPath, directory, directory);
			}

			return HTTPStatus.NOT_FOUND;
		}

		var path = directory.resolve(pathVar);

		if (path.startsWith(directory) && Files.exists(path) && Files.isReadable(path)) {
			if (Files.isRegularFile(path)) {
				return HTTPResponse.ok().publicCache(cacheDuration).content(path);
			} else if (autoIndex && Files.isDirectory(path)) {
				return index(httpPath, directory, path);
			}
		}

		return HTTPStatus.NOT_FOUND;
	}

	private static HTTPResponse index(String httpPath, Path rootDirectory, Path directory) throws IOException {
		var sb = new StringBuilder();
		sb.append("<ul>");

		if (!rootDirectory.equals(directory)) {
			sb.append("<li><a href=\"").append(httpPath).append('/').append(rootDirectory.relativize(directory.getParent())).append("\">..</a></li>");
		}

		for (var file : Files.list(directory).sorted().toList()) {
			var name = file.getFileName().toString();
			sb.append("<li><a href=\"").append(httpPath).append('/').append(rootDirectory.relativize(file)).append("\">").append(name).append("</a></li>");
		}

		sb.append("</ul>");

		return HTTPResponse.ok().content(sb, MimeType.HTML);
	}
}
