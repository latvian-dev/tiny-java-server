package dev.latvian.apps.tinyserver.http.response.error;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class BadRequestError extends HTTPError {
	public BadRequestError(String message) {
		super(HTTPStatus.BAD_REQUEST, message);
	}

	public BadRequestError(String message, Throwable cause) {
		super(HTTPStatus.BAD_REQUEST, message, cause);
	}
}
