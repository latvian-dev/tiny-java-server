package dev.latvian.apps.tinyserver.http.response.error.client;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class BadRequestError extends ClientError {
	public BadRequestError() {
	}

	public BadRequestError(String message) {
		super(message);
	}

	public BadRequestError(String message, Throwable cause) {
		super(message, cause);
	}

	public BadRequestError(Throwable cause) {
		super(cause);
	}

	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.BAD_REQUEST;
	}
}
