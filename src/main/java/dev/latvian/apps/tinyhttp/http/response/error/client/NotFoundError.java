package dev.latvian.apps.tinyhttp.http.response.error.client;

import dev.latvian.apps.tinyhttp.http.response.HTTPStatus;

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
