package dev.latvian.apps.tinyserver.http.response;

import dev.latvian.apps.tinyserver.http.HTTPUpgrade;

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
