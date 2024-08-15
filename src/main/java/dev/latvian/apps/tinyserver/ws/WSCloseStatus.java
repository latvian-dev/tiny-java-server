package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.StatusCode;

public enum WSCloseStatus {
	CLOSED(1000, "Closed"),
	GOING_AWAY(1001, "Going Away"),
	PROTOCOL_ERROR(1002, "Protocol Error"),
	UNSUPPORTED_DATA(1003, "Unsupported Data"),

	;

	public final StatusCode statusCode;

	WSCloseStatus(int code, String reason) {
		this.statusCode = new StatusCode(code, reason);
	}
}
