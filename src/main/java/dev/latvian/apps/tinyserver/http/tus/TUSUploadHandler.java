package dev.latvian.apps.tinyserver.http.tus;

import dev.latvian.apps.tinyserver.ServerRegistry;
import dev.latvian.apps.tinyserver.content.MimeType;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.body.Body;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;
import dev.latvian.apps.tinyserver.http.response.error.client.ContentTooLargeError;
import dev.latvian.apps.tinyserver.http.response.error.client.UnsupportedMediaTypeError;

public interface TUSUploadHandler<REQ extends HTTPRequest, DATA> {
	static <REQ extends HTTPRequest, DATA> void register(ServerRegistry<REQ> server, String path, TUSUploadHandler<REQ, DATA> handler) {
		server.options(path, (req, headers) -> {
			headers.addHeader("Tus-Resumable", "1.0.0");
			headers.addHeader("Tus-Version", "1.0.0,0.2.2,0.2.1");
			headers.addUnsignedHeader("Tus-Max-Size", handler.getMaxChunkSize(req));
		});

		server.get(path, req -> handler.getResponse(req, handler.createData(req)));
		server.patch(path, req -> handler.patchResponse(req, handler.createData(req)));

		if (handler.handleTermination()) {
			server.delete(path, req -> handler.deleteResponse(req, handler.createData(req)));
		}
	}

	record UploadData(long offset) {
	}

	default int getMaxChunkSize(REQ req) {
		return 52428800; // 50 MiB
	}

	DATA createData(REQ req) throws Exception;

	long getSize(REQ req, DATA data) throws Exception;

	long getOffset(REQ req, DATA data) throws Exception;

	UploadData write(REQ req, DATA data, Body body) throws Exception;

	default boolean handleTermination() {
		return false;
	}

	default boolean terminate(REQ req, DATA data) throws Exception {
		return false;
	}

	default HTTPResponse getResponse(REQ req, DATA data) throws Exception {
		var offset = getOffset(req, data);
		var size = getSize(req, data);

		return HTTPResponse.ok()
			.header("Upload-Offset", offset)
			.header("Upload-Length", size)
			.header("Tus-Resumable", "1.0.0")
			.noCache();
	}

	default HTTPResponse patchResponse(REQ req, DATA data) throws Exception {
		var body = req.mainBody();

		if (!body.contentType().equals(MimeType.OFFSET_OCTET_STREAM)) {
			throw new UnsupportedMediaTypeError();
		} else if (body.bytes().length > getMaxChunkSize(req)) {
			throw new ContentTooLargeError();
		}

		var result = write(req, data, body);

		return HTTPResponse.noContent()
			.header("Upload-Offset", Long.toUnsignedString(result.offset))
			.header("Tus-Resumable", "1.0.0");
	}

	default HTTPResponse deleteResponse(REQ req, DATA data) throws Exception {
		if (terminate(req, data)) {
			return HTTPResponse.noContent().header("Tus-Resumable", "1.0.0");
		} else {
			return HTTPStatus.GONE.header("Tus-Resumable", "1.0.0");
		}
	}
}