package dev.latvian.apps.tinyserver.http.tus;

import dev.latvian.apps.tinyserver.ServerRegistry;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.response.HTTPPayload;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.util.Base64EncodedMetadata;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;

public interface TUSCreationHandler<REQ extends HTTPRequest> {
	static <REQ extends HTTPRequest> void register(ServerRegistry<REQ> server, String path, TUSCreationHandler<REQ> handler) {
		server.options(path, (req, headers) -> {
			headers.addHeader("Tus-Resumable", "1.0.0");
			headers.addHeader("Tus-Version", "1.0.0,0.2.2,0.2.1");
			headers.addUnsignedHeader("Tus-Max-Size", handler.getMaxChunkSize(req));

			var extensions = new ArrayList<String>(3);
			extensions.add("creation");

			var expires = handler.getExpiresAt(req);

			if (expires != null) {
				extensions.add("expiration");
				headers.addHeader("Upload-Expires", HTTPPayload.DATE_TIME_FORMATTER.format(expires));
			}

			// checksum
			// Tus-Checksum-Algorithm: md5,sha1,crc32

			headers.addHeader("Tus-Extension", String.join(",", extensions));
		});

		server.post(path, req -> {
			long uploadLength = req.header("Upload-Length").asULong();
			var uploadMetadata = req.header("Upload-Metadata").asBase64EncodedMetadata();

			var result = handler.createUpload(req, uploadLength, uploadMetadata);

			return HTTPResponse.created()
				.header("Location", result.location())
				.header("Tus-Resumable", "1.0.0");
		});
	}

	record CreationData(String location) {
	}

	default int getMaxChunkSize(REQ req) {
		return 52428800; // 50 MiB
	}

	@Nullable
	default Instant getExpiresAt(REQ req) {
		return null;
	}

	CreationData createUpload(REQ request, long uploadLength, Base64EncodedMetadata uploadMetadata) throws Exception;
}