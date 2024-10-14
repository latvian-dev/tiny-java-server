package dev.latvian.apps.tinyserver.http.response.error.client;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class ForbiddenError extends ClientError {
	public ForbiddenError() {
	}

	public ForbiddenError(String message) {
		super(message);
	}

	public ForbiddenError(String message, Throwable cause) {
		super(message, cause);
	}

	public ForbiddenError(Throwable cause) {
		super(cause);
	}

	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.FORBIDDEN;
	}
}
