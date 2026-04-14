package dev.latvian.apps.tinyserver.http.response.error.client;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class UnprocessableContentError extends ClientError {
	public UnprocessableContentError() {
	}

	public UnprocessableContentError(String message) {
		super(message);
	}

	public UnprocessableContentError(String message, Throwable cause) {
		super(message, cause);
	}

	public UnprocessableContentError(Throwable cause) {
		super(cause);
	}

	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.UNPROCESSABLE_CONTENT;
	}
}
