package dev.latvian.apps.tinyserver.http.response.error;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class ForbiddenError extends HTTPError {
	public ForbiddenError(String message) {
		super(HTTPStatus.FORBIDDEN, message);
	}

	public ForbiddenError(String message, Throwable cause) {
		super(HTTPStatus.FORBIDDEN, message, cause);
	}
}
