package dev.latvian.apps.tinyhttp.http.response;

import dev.latvian.apps.tinyhttp.content.ByteContent;
import dev.latvian.apps.tinyhttp.content.FileContent;
import dev.latvian.apps.tinyhttp.content.LazyContent;
import dev.latvian.apps.tinyhttp.content.MimeType;
import dev.latvian.apps.tinyhttp.content.ResponseContent;
import dev.latvian.apps.tinyhttp.http.HTTPUpgrade;
import dev.latvian.apps.tinyhttp.http.body.Body;
import dev.latvian.apps.tinyhttp.http.response.error.client.NotFoundError;
import dev.latvian.apps.tinyhttp.http.response.error.server.InternalError;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.function.UnaryOperator;

public interface HTTPResponse {
	static HTTPResponse ok() {
		return HTTPStatus.OK;
	}

	static HTTPResponse created() {
		return HTTPStatus.CREATED;
	}

	static HTTPResponse accepted() {
		return HTTPStatus.ACCEPTED;
	}

	static HTTPResponse noContent() {
		return HTTPStatus.NO_CONTENT;
	}

	static HTTPResponse redirect(String location) {
		return new RedirectResponse(HTTPStatus.FOUND, location);
	}

	static HTTPResponse movedPermanently(String location) {
		return new RedirectResponse(HTTPStatus.MOVED_PERMANENTLY, location);
	}

	static HTTPResponse redirectTemporary(String location) {
		return new RedirectResponse(HTTPStatus.TEMPORARY_REDIRECT, location);
	}

	static HTTPResponse redirectPermanently(String location) {
		return new RedirectResponse(HTTPStatus.PERMANENT_REDIRECT, location);
	}

	static HTTPResponse upgrade(HTTPUpgrade<?> upgrade) {
		return new UpgradeResponse(upgrade);
	}

	HTTPStatus status();

	void build(HTTPPayload payload);

	default HTTPResponse headers(Map<String, ?> headers) {
		var res = this;

		for (var entry : headers.entrySet()) {
			var key = entry.getKey();
			var value = entry.getValue();
			res = res.header(key, value);
		}

		return res;
	}

	default HTTPResponse header(String header, Object value) {
		return new HeaderResponse(this, header, String.valueOf(value));
	}

	default HTTPResponse cookie(String key, String value) {
		return new CookieResponse(this, key, value);
	}

	default HTTPResponse cookie(String key, String value, UnaryOperator<CookieResponse.Builder> properties) {
		return new CookieResponse(this, key, value, properties.apply(new CookieResponse.Builder()));
	}

	default HTTPResponse removeCookie(String key) {
		return new CookieResponse(this, key, "", new CookieResponse.Builder().remove());
	}

	default HTTPResponse cache(boolean isPublic, Duration duration) {
		return new CacheControlResponse(this, isPublic, duration);
	}

	default HTTPResponse noCache() {
		return cache(true, Duration.ZERO);
	}

	default HTTPResponse publicCache(Duration duration) {
		return cache(true, duration);
	}

	default HTTPResponse privateCache(Duration duration) {
		return cache(false, duration);
	}

	default HTTPResponse cors(String value) {
		return new CORSResponse(this, value);
	}

	default HTTPResponse cors() {
		return cors("*");
	}

	default HTTPResponse content(ResponseContent content) {
		return new ContentResponse(this, content);
	}

	default HTTPResponse content(byte[] bytes, String type) {
		return new ContentResponse(this, new ByteContent(bytes, type));
	}

	default HTTPResponse content(CharSequence string, String type) {
		return new ContentResponse(this, new ByteContent(String.valueOf(string).getBytes(StandardCharsets.UTF_8), type));
	}

	default HTTPResponse content(Path file, String overrideType) throws IOException {
		if (Files.exists(file) && Files.isRegularFile(file) && Files.isReadable(file)) {
			return content(new FileContent(file, overrideType));
		} else {
			throw new NotFoundError();
		}
	}

	default HTTPResponse content(Path file) throws IOException {
		return content(file, "");
	}

	default HTTPResponse html(String text) {
		return content(text, MimeType.HTML).compress();
	}

	default HTTPResponse text(String text) {
		return content(text, MimeType.TEXT).compress();
	}

	default HTTPResponse text(Iterable<String> text) {
		return text(String.join("\n", text));
	}

	default HTTPResponse json(String json) {
		return content(json, MimeType.JSON).compress();
	}

	default HTTPResponse png(RenderedImage img) {
		return content(new LazyContent(() -> {
			try {
				var bytes = new ByteArrayOutputStream();
				ImageIO.write(img, "png", bytes);
				return new ByteContent(bytes.toByteArray(), MimeType.PNG);
			} catch (Exception ex) {
				throw new InternalError(ex);
			}
		}));
	}

	default HTTPResponse jpeg(RenderedImage img) {
		return content(new LazyContent(() -> {
			try {
				var bytes = new ByteArrayOutputStream();
				ImageIO.write(img, "jpeg", bytes);
				return new ByteContent(bytes.toByteArray(), MimeType.JPEG);
			} catch (Exception ex) {
				throw new InternalError(ex);
			}
		}));
	}

	default HTTPResponse encode(String method) {
		return new EncodeResponse(this, method);
	}

	default HTTPResponse compress() {
		return encode("");
	}

	default HTTPResponse download(String fileName) {
		var builder = new StringBuilder(23 + fileName.length());
		builder.append("attachment; filename=");
		Body.appendQuotedString(builder, fileName);
		return header("Content-Disposition", builder.toString());
	}

	default HTTPResponse acceptRanges(String ranges) {
		return new AcceptRangesResponse(this, ranges);
	}

	default HTTPResponse acceptByteRanges() {
		return acceptRanges("bytes");
	}

	default HTTPResponse contentRange(int start, int end, int total) {
		return header("Content-Range", "bytes " + start + "-" + end + "/" + total);
	}

	default HTTPResponse fullContentRange(int total) {
		return header("Content-Range", "bytes */" + total);
	}

	default HTTPResponse strongETag(String etag) {
		return header("ETag", "\"" + etag + "\"");
	}

	default HTTPResponse weakETag(String etag) {
		return header("ETag", "W/\"" + etag + "\"");
	}

	default HTTPResponse lastModified(TemporalAccessor time) {
		return header("Last-Modified", HTTPPayload.DATE_TIME_FORMATTER.format(time));
	}
}
