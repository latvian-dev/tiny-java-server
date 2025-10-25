package dev.latvian.apps.tinyserver.http.file;

import dev.latvian.apps.tinyserver.content.MimeType;
import dev.latvian.apps.tinyserver.http.HTTPHandler;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileIndexHandler<REQ extends HTTPRequest> implements HTTPHandler<REQ> {
	public final Path rootDirectory;
	public final Path directory;
	public final FileResponseHandler responseHandler;

	public FileIndexHandler(Path rootDirectory, Path directory, FileResponseHandler responseHandler) {
		this.rootDirectory = rootDirectory;
		this.directory = directory;
		this.responseHandler = responseHandler;
	}

	@Override
	public HTTPResponse handle(REQ req) throws IOException {
		if (Files.exists(directory) && Files.isReadable(directory) && Files.isDirectory(directory)) {
			return responseHandler.apply(index("/" + req.path(), rootDirectory, directory), true, directory);
		}

		return HTTPStatus.NOT_FOUND;
	}

	public static HTTPResponse index(String httpPath, Path rootDirectory, Path directory) throws IOException {
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

	@Override
	public boolean isFileHandler() {
		return true;
	}
}
