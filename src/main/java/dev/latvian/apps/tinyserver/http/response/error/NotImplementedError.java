package dev.latvian.apps.tinyserver.http.response.error;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class NotImplementedError extends HTTPError {
	public NotImplementedError(String message) {
		super(HTTPStatus.NOT_IMPLEMENTED, message);
	}

	public NotImplementedError(String message, Throwable cause) {
		super(HTTPStatus.NOT_IMPLEMENTED, message, cause);
	}
}
