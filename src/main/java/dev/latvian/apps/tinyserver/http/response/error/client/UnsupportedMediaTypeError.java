package dev.latvian.apps.tinyserver.http.response.error.client;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class UnsupportedMediaTypeError extends ClientError {
	public UnsupportedMediaTypeError() {
	}

	public UnsupportedMediaTypeError(String message) {
		super(message);
	}

	public UnsupportedMediaTypeError(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedMediaTypeError(Throwable cause) {
		super(cause);
	}

	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.UNSUPPORTED_MEDIA_TYPE;
	}
}
