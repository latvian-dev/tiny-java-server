package dev.latvian.apps.tinyserver.http.response.error;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class HTTPError extends RuntimeException {
	private final HTTPStatus status;
	private Object extraData;

	public HTTPError(HTTPStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HTTPError(HTTPStatus status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public HTTPStatus getStatus() {
		return status;
	}

	public Object getExtraData() {
		return extraData;
	}

	public HTTPError withExtraData(Object extraData) {
		this.extraData = extraData;
		return this;
	}
}
