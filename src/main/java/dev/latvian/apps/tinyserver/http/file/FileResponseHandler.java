package dev.latvian.apps.tinyserver.http.file;

import dev.latvian.apps.tinyserver.http.response.HTTPResponse;

import java.nio.file.Path;
import java.time.Duration;

@FunctionalInterface
public interface FileResponseHandler {
	record PublicCache(Duration duration, boolean gzip) implements FileResponseHandler {
		@Override
		public HTTPResponse apply(HTTPResponse response, boolean directory, Path path) {
			if (!directory) {
				if (duration.isZero()) {
					response = response.noCache();
				}

				if (gzip) {
					response = response.gzip();
				}
			}

			return response;
		}
	}

	static FileResponseHandler publicCache(Duration duration, boolean gzip) {
		return new PublicCache(duration, gzip);
	}

	static FileResponseHandler publicCache(long minutes, boolean gzip) {
		return publicCache(Duration.ofMinutes(minutes), gzip);
	}

	HTTPResponse apply(HTTPResponse response, boolean directory, Path path);
}
