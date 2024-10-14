package dev.latvian.apps.tinyserver.http.response.error;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public abstract class HTTPError extends RuntimeException {
	private Object extraData;

	public HTTPError() {
	}

	public HTTPError(String message) {
		super(message);
	}

	public HTTPError(String message, Throwable cause) {
		super(message, cause);
	}

	public HTTPError(Throwable cause) {
		super(cause);
	}

	public abstract HTTPStatus getStatus();

	public Object getExtraData() {
		return extraData;
	}

	public HTTPError withExtraData(Object extraData) {
		this.extraData = extraData;
		return this;
	}
}
