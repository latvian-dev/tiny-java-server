package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.StatusCode;

public enum WSCloseStatus {
	CLOSED(1000, "Closed"),
	GOING_AWAY(1001, "Going Away"),
	PROTOCOL_ERROR(1002, "Protocol Error"),
	UNSUPPORTED_DATA(1003, "Unsupported Data"),
	ABNORMAL_CLOSURE(1006, "Abnormal Closure"),
	INVALID_FRAME_PAYLOAD_DATA(1007, "Invalid Frame Payload Data"),
	INTERNAL_ERROR(1011, "Internal Error"),
	SERVICE_RESTART(1012, "Service Restart"),

	;

	public final StatusCode statusCode;

	WSCloseStatus(int code, String reason) {
		this.statusCode = new StatusCode(code, reason);
	}
}
