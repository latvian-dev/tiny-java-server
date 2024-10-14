package dev.latvian.apps.tinyserver.http.response.error.server;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class NotImplementedError extends ServerError {
	public NotImplementedError() {
	}

	public NotImplementedError(String message) {
		super(message);
	}

	public NotImplementedError(String message, Throwable cause) {
		super(message, cause);
	}

	public NotImplementedError(Throwable cause) {
		super(cause);
	}

	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.NOT_IMPLEMENTED;
	}
}
