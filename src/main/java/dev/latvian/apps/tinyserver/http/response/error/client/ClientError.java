package dev.latvian.apps.tinyserver.http.response.error.client;

import dev.latvian.apps.tinyserver.http.response.error.HTTPError;

public abstract class ClientError extends HTTPError {
	public ClientError() {
	}

	public ClientError(String message) {
		super(message);
	}

	public ClientError(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientError(Throwable cause) {
		super(cause);
	}
}
