package dev.latvian.apps.tinyserver.http.response.error.client;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class UnauthorizedError extends ClientError {
	public UnauthorizedError() {
	}

	public UnauthorizedError(String message) {
		super(message);
	}

	public UnauthorizedError(String message, Throwable cause) {
		super(message, cause);
	}

	public UnauthorizedError(Throwable cause) {
		super(cause);
	}

	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.UNAUTHORIZED;
	}
}
