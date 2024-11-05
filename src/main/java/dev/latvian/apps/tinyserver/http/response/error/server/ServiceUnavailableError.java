package dev.latvian.apps.tinyserver.http.response.error.server;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class ServiceUnavailableError extends ServerError {
	public ServiceUnavailableError() {
	}

	public ServiceUnavailableError(String message) {
		super(message);
	}

	public ServiceUnavailableError(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceUnavailableError(Throwable cause) {
		super(cause);
	}

	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.SERVICE_UNAVAILABLE;
	}
}
