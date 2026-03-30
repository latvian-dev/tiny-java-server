package dev.latvian.apps.tinyserver.http.response.error.client;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class ContentTooLargeError extends ClientError {
	public ContentTooLargeError() {
	}

	public ContentTooLargeError(String message) {
		super(message);
	}

	public ContentTooLargeError(String message, Throwable cause) {
		super(message, cause);
	}

	public ContentTooLargeError(Throwable cause) {
		super(cause);
	}

	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.CONTENT_TOO_LARGE;
	}
}
