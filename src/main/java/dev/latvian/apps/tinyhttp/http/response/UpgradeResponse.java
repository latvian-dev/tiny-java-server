package dev.latvian.apps.tinyhttp.http.response;

import dev.latvian.apps.tinyhttp.http.HTTPUpgrade;

public record UpgradeResponse(HTTPUpgrade<?> upgrade) implements HTTPResponse {
	@Override
	public HTTPStatus status() {
		return HTTPStatus.SWITCHING_PROTOCOLS;
	}

	@Override
	public void build(HTTPPayload payload) {
		payload.setUpgrade(upgrade);
	}
}
