package dev.latvian.apps.tinyserver.http.response.error;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class UnauthorizedError extends HTTPError {
	public UnauthorizedError(String message) {
		super(HTTPStatus.UNAUTHORIZED, message);
	}

	public UnauthorizedError(String message, Throwable cause) {
		super(HTTPStatus.UNAUTHORIZED, message, cause);
	}
}
