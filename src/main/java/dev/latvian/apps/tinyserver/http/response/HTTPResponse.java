package dev.latvian.apps.tinyserver.http.response;

import dev.latvian.apps.tinyserver.content.ByteContent;
import dev.latvian.apps.tinyserver.content.FileContent;
import dev.latvian.apps.tinyserver.content.LazyContent;
import dev.latvian.apps.tinyserver.content.MimeType;
import dev.latvian.apps.tinyserver.content.ResponseContent;
import dev.latvian.apps.tinyserver.http.HTTPUpgrade;
import dev.latvian.apps.tinyserver.http.response.encoding.DeflateResponseContentEncoding;
import dev.latvian.apps.tinyserver.http.response.encoding.GZIPResponseContentEncoding;
import dev.latvian.apps.tinyserver.http.response.encoding.ResponseContentEncoding;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
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

	default HTTPResponse content(Path file, String overrideType) {
		return content(new FileContent(file, overrideType));
	}

	default HTTPResponse content(Path file) {
		return content(file, "");
	}

	default HTTPResponse html(String text) {
		return content(text, MimeType.HTML).gzip();
	}

	default HTTPResponse text(String text) {
		return content(text, MimeType.TEXT).gzip();
	}

	default HTTPResponse text(Iterable<String> text) {
		return text(String.join("\n", text));
	}

	default HTTPResponse json(String json) {
		return content(json, MimeType.JSON).gzip();
	}

	default HTTPResponse png(BufferedImage img) {
		return content(new LazyContent(() -> {
			try {
				var bytes = new ByteArrayOutputStream();
				ImageIO.write(img, "png", bytes);
				return new ByteContent(bytes.toByteArray(), MimeType.PNG);
			} catch (Exception ex) {
				return ByteContent.EMPTY;
			}
		})).gzip();
	}

	default HTTPResponse jpeg(BufferedImage img) {
		return content(new LazyContent(() -> {
			try {
				var bytes = new ByteArrayOutputStream();
				ImageIO.write(img, "jpeg", bytes);
				return new ByteContent(bytes.toByteArray(), MimeType.JPEG);
			} catch (Exception ex) {
				return ByteContent.EMPTY;
			}
		})).gzip();
	}

	default HTTPResponse encoding(ResponseContentEncoding encoding) {
		return new EncodingResponse(this, encoding);
	}

	default HTTPResponse gzip() {
		return encoding(GZIPResponseContentEncoding.INSTANCE);
	}

	default HTTPResponse deflate() {
		return encoding(DeflateResponseContentEncoding.INSTANCE);
	}
}
