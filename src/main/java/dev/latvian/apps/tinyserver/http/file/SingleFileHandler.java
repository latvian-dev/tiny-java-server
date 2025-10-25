package dev.latvian.apps.tinyserver.http.file;

import dev.latvian.apps.tinyserver.http.HTTPHandler;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

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
	public HTTPResponse handle(REQ req) {
		if (Files.exists(path) && Files.isReadable(path) && Files.isRegularFile(path)) {
			return responseHandler.apply(HTTPResponse.ok().content(path, contentType), false, path);
		}

		return HTTPStatus.NOT_FOUND;
	}

	@Override
	public boolean isFileHandler() {
		return true;
	}
}
