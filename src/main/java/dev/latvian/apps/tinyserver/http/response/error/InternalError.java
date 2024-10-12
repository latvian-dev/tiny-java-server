package dev.latvian.apps.tinyserver.http.response.error;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class InternalError extends HTTPError {
	public InternalError(String message) {
		super(HTTPStatus.INTERNAL_ERROR, message);
	}

	public InternalError(String message, Throwable cause) {
		super(HTTPStatus.INTERNAL_ERROR, message, cause);
	}
}
