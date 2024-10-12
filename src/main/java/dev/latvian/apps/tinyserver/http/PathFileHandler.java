package dev.latvian.apps.tinyserver.http;

import dev.latvian.apps.tinyserver.content.MimeType;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public record PathFileHandler<REQ extends HTTPRequest>(String httpPath, Path directory, Duration cacheDuration, boolean autoInedx) implements HTTPHandler<REQ> {
	@Override
	public HTTPResponse handle(REQ req) throws IOException {
		var path = directory.resolve(req.variable("path"));

		if (path.startsWith(directory) && Files.exists(path) && Files.isReadable(path)) {
			if (Files.isRegularFile(path)) {
				return HTTPResponse.ok().publicCache(cacheDuration).content(path);
			} else if (autoInedx && Files.isDirectory(path)) {
				return index(httpPath, directory, path);
			}
		}

		return HTTPStatus.NOT_FOUND;
	}

	public static HTTPResponse index(String httpPath, Path rootDirectory, Path directory) throws IOException {
		var sb = new StringBuilder();
		sb.append("<ul>");

		if (!rootDirectory.equals(directory)) {
			sb.append("<li><a href=\"" + httpPath + "/" + rootDirectory.relativize(directory.getParent()) + "\">..</a></li>");
		}

		for (var file : Files.list(directory).sorted().toList()) {
			var name = file.getFileName().toString();
			sb.append("<li><a href=\"" + httpPath + "/" + rootDirectory.relativize(file) + "\">" + name + "</a></li>");
		}

		sb.append("</ul>");

		return HTTPResponse.ok().content(sb.toString().getBytes(StandardCharsets.UTF_8), MimeType.HTML);
	}
}
