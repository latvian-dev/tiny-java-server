package dev.latvian.apps.tinyserver.http.response;

import dev.latvian.apps.tinyserver.content.ByteContent;
import dev.latvian.apps.tinyserver.content.FileContent;
import dev.latvian.apps.tinyserver.content.ResponseContent;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

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

	void build(HTTPResponseBuilder payload) throws Exception;

	default HTTPResponse header(String header, String value) {
		return new HTTPResponseWithHeader(this, header, value);
	}

	default HTTPResponse noCache() {
		return header("Cache-Control", "no-cache, no-store, must-revalidate");
	}

	default HTTPResponse publicCache(int seconds) {
		return header("Cache-Control", "public, max-age=" + seconds);
	}

	default HTTPResponse privateCache(int seconds) {
		return header("Cache-Control", "private, max-age=" + seconds);
	}

	default HTTPResponse content(ResponseContent content) {
		return new ContentResponse(this, content);
	}

	default HTTPResponse content(byte[] bytes, String type) {
		return new ContentResponse(this, new ByteContent(bytes, type));
	}

	default HTTPResponse content(Path file, String overrideType) {
		return content(new FileContent(file, overrideType));
	}

	default HTTPResponse content(Path file) {
		return content(file, "");
	}

	default HTTPResponse text(String text) {
		return content(text.getBytes(StandardCharsets.UTF_8), "text/plain; charset=utf-8");
	}

	default HTTPResponse text(Iterable<String> text) {
		return text(String.join("\n", text));
	}

	default HTTPResponse json(String json) {
		return content(json.getBytes(StandardCharsets.UTF_8), "application/json; charset=utf-8");
	}


}
