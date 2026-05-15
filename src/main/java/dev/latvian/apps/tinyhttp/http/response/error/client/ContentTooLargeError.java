package dev.latvian.apps.tinyhttp.http.response.error.client;

import dev.latvian.apps.tinyhttp.http.response.HTTPStatus;

public class ContentTooLargeError extends ClientError {
	public long size;
	public long maxSize;

	public ContentTooLargeError(long size, long maxSize) {
		this.size = size;
		this.maxSize = maxSize;
	}

	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.CONTENT_TOO_LARGE;
	}
}
