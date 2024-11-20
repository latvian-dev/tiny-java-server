package dev.latvian.apps.tinyserver.http.file;

import dev.latvian.apps.tinyserver.http.response.HTTPResponse;

import java.nio.file.Path;
import java.time.Duration;

@FunctionalInterface
public interface FileResponseHandler {
	record Cache(Duration duration, Duration styleAndScriptDuration, boolean gzip) implements FileResponseHandler {
		@Override
		public HTTPResponse apply(HTTPResponse response, boolean directory, Path path) {
			if (!directory) {
				var n = path.getFileName().toString();
				response = response.publicCache(n.endsWith(".css") || n.endsWith(".js") ? styleAndScriptDuration : duration);

				if (gzip) {
					response = response.gzip();
				}
			}

			return response;
		}
	}

	FileResponseHandler CACHE_5_MIN = cache(Duration.ofMinutes(5L));
	FileResponseHandler CACHE_1_HOUR = cache(Duration.ofHours(1L));

	static FileResponseHandler cache(Duration duration, Duration styleAndScriptDuration, boolean gzip) {
		return new Cache(duration, styleAndScriptDuration, gzip);
	}

	static FileResponseHandler cache(Duration duration) {
		return new Cache(duration, duration, true);
	}

	HTTPResponse apply(HTTPResponse response, boolean directory, Path path);
}
