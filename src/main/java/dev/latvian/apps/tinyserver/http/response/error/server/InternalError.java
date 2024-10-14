package dev.latvian.apps.tinyserver.http.response.error.server;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class InternalError extends ServerError {
	public InternalError() {
	}

	public InternalError(String message) {
		super(message);
	}

	public InternalError(String message, Throwable cause) {
		super(message, cause);
	}

	public InternalError(Throwable cause) {
		super(cause);
	}

	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.INTERNAL_ERROR;
	}
}
