package dev.latvian.apps.tinyserver.http.response;

import dev.latvian.apps.tinyserver.content.ByteContent;
import dev.latvian.apps.tinyserver.content.FileContent;
import dev.latvian.apps.tinyserver.content.MimeType;
import dev.latvian.apps.tinyserver.content.ResponseContent;
import dev.latvian.apps.tinyserver.http.response.encoding.DeflateResponseContentEncoding;
import dev.latvian.apps.tinyserver.http.response.encoding.GZIPResponseContentEncoding;
import dev.latvian.apps.tinyserver.http.response.encoding.ResponseContentEncoding;

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
		return new RedirectResponse(EmptyResponse.INSTANCE, HTTPStatus.FOUND, location);
	}

	static HTTPResponse movedPermanently(String location) {
		return new RedirectResponse(EmptyResponse.INSTANCE, HTTPStatus.MOVED_PERMANENTLY, location);
	}

	static HTTPResponse redirectTemporary(String location) {
		return new RedirectResponse(EmptyResponse.INSTANCE, HTTPStatus.TEMPORARY_REDIRECT, location);
	}

	static HTTPResponse redirectPermanently(String location) {
		return new RedirectResponse(EmptyResponse.INSTANCE, HTTPStatus.PERMANENT_REDIRECT, location);
	}

	void build(HTTPPayload payload) throws Exception;

	default HTTPResponse header(String header, Object value) {
		return new HTTPResponseWithHeader(this, header, String.valueOf(value));
	}

	default HTTPResponse cookie(String key, String value) {
		return new HTTPResponseWithCookie(this, key, value);
	}

	default HTTPResponse cookie(String key, String value, UnaryOperator<HTTPResponseWithCookie.Builder> properties) {
		return new HTTPResponseWithCookie(this, key, value, properties.apply(new HTTPResponseWithCookie.Builder()));
	}

	default HTTPResponse removeCookie(String key) {
		return new HTTPResponseWithCookie(this, key, "", new HTTPResponseWithCookie.Builder().remove());
	}

	default HTTPResponse cache(boolean isPublic, Duration duration) {
		return new HTTPResponseWithCacheControl(this, isPublic, duration);
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

	default HTTPResponse content(ResponseContent content) {
		return new ContentResponse(this, content);
	}

	default HTTPResponse content(byte[] bytes, String type) {
		return new ContentResponse(this, new ByteContent(bytes, type));
	}

	default HTTPResponse content(CharSequence string, String type) {
		return new ContentResponse(this, new ByteContent(string.toString().getBytes(StandardCharsets.UTF_8), type));
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

	default HTTPResponse encoding(ResponseContentEncoding encoding) {
		return new HTTPResponseWithEncoding(this, encoding);
	}

	default HTTPResponse gzip() {
		return encoding(GZIPResponseContentEncoding.INSTANCE);
	}

	default HTTPResponse deflate() {
		return encoding(DeflateResponseContentEncoding.INSTANCE);
	}
}
