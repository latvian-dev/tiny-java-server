package dev.latvian.apps.tinyserver.http.response.error;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class NotFoundError extends HTTPError {
	public NotFoundError(String message) {
		super(HTTPStatus.NOT_FOUND, message);
	}

	public NotFoundError(String message, Throwable cause) {
		super(HTTPStatus.NOT_FOUND, message, cause);
	}

	public NotFoundError(Throwable cause) {
		super(HTTPStatus.NOT_FOUND, cause);
	}
}
