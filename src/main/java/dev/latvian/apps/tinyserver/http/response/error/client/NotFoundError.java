package dev.latvian.apps.tinyserver.http.response.error.client;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class NotFoundError extends ClientError {
	public NotFoundError() {
	}

	public NotFoundError(String message) {
		super(message);
	}

	public NotFoundError(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundError(Throwable cause) {
		super(cause);
	}

	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.NOT_FOUND;
	}
}
