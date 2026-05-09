package dev.latvian.apps.tinyhttp.http.response.error.client;

import dev.latvian.apps.tinyhttp.http.response.HTTPStatus;

public class LengthRequiredError extends ClientError {
	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.LENGTH_REQUIRED;
	}
}
