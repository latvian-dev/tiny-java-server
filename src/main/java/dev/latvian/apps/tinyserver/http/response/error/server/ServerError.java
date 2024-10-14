package dev.latvian.apps.tinyserver.http.response.error.server;

import dev.latvian.apps.tinyserver.http.response.error.HTTPError;

public abstract class ServerError extends HTTPError {
	public ServerError() {
	}

	public ServerError(String message) {
		super(message);
	}

	public ServerError(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerError(Throwable cause) {
		super(cause);
	}
}
