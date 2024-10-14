package dev.latvian.apps.tinyserver.http.response.error.client;

import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public class LengthRequiredError extends ClientError {
	@Override
	public HTTPStatus getStatus() {
		return HTTPStatus.LENGTH_REQUIRED;
	}
}
