package dev.latvian.apps.tinyhttp.http.response.error.client;

import dev.latvian.apps.tinyhttp.http.response.HTTPStatus;

public class RangeNotSatisfiableError extends ClientError {
	public final long length;

	public RangeNotSatisfiableError(long length) {
		this.length = length;
	}

	public RangeNotSatisfiableError(long length, String message) {
		super(message);
		this.length = length;
	}

	public RangeNotSatisfiableError(long length, String message, Throwable cause) {
		super(message, cause);
		this.length = length;
	}

	public RangeNotSatisfiableError(long length, Throwable cause) {
		super(cause);
		this.length = length;
	}

	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.RANGE_NOT_SATISFIABLE;
	}
}
