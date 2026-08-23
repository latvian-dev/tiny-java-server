package dev.latvian.apps.tinyhttp.http.file;

import dev.latvian.apps.tinyhttp.http.HTTPHandler;
import dev.latvian.apps.tinyhttp.http.HTTPRequest;
import dev.latvian.apps.tinyhttp.http.response.HTTPResponse;
import dev.latvian.apps.tinyhttp.http.response.HTTPStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SingleFileHandler<REQ extends HTTPRequest> implements HTTPHandler<REQ> {
	public final Path path;
	public final String contentType;
	public final FileResponseHandler responseHandler;

	public SingleFileHandler(Path path, String contentType, FileResponseHandler responseHandler) {
		this.path = path;
		this.contentType = contentType;
		this.responseHandler = responseHandler;
	}

	@Override
	public HTTPResponse handle(REQ req) throws IOException {
		if (Files.exists(path) && Files.isReadable(path) && Files.isRegularFile(path)) {
			var res = responseHandler.apply(HTTPResponse.ok().content(path, contentType), false, path);

			try {
				var lastModified = Files.getLastModifiedTime(path);

				if (lastModified != null) {
					var instant = lastModified.toInstant();
					res = res.lastModified(instant).strongETag("%08x%04x".formatted(instant.getEpochSecond(), instant.getNano()));
				}
			} catch (Exception ignored) {
			}

			return res;
		}

		return HTTPStatus.NOT_FOUND;
	}

	@Override
	public boolean isFileHandler() {
		return true;
	}
}
