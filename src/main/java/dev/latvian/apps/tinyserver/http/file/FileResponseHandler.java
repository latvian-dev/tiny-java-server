package dev.latvian.apps.tinyserver.http.file;

import dev.latvian.apps.tinyserver.http.response.HTTPResponse;

import java.nio.file.Path;
import java.time.Duration;

@FunctionalInterface
public interface FileResponseHandler {
	record Cache(Duration duration, boolean gzip, boolean cacheStyleAndScripts) implements FileResponseHandler {
		@Override
		public HTTPResponse apply(HTTPResponse response, boolean directory, Path path) {
			if (!directory) {
				if (!duration.isZero()) {
					if (cacheStyleAndScripts) {
						response = response.publicCache(duration);
					} else {
						var n = path.toString();

						if (n.endsWith(".css") || n.endsWith(".js")) {
							response = response.noCache();
						} else {
							response = response.publicCache(duration);
						}
					}
				}

				if (gzip) {
					response = response.gzip();
				}
			}

			return response;
		}
	}

	FileResponseHandler CACHE_5_MIN = cache(Duration.ofMinutes(5L));
	FileResponseHandler CACHE_1_HOUR = cache(Duration.ofHours(1L));

	static FileResponseHandler cache(Duration duration, boolean gzip, boolean cacheStyleAndScripts) {
		return new Cache(duration, gzip, cacheStyleAndScripts);
	}

	static FileResponseHandler cache(Duration duration) {
		return new Cache(duration, true, false);
	}

	HTTPResponse apply(HTTPResponse response, boolean directory, Path path);
}
