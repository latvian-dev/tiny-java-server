package dev.latvian.apps.tinyserver.http;

public enum HTTPMethod {
	HEAD(false),
	GET(true),
	POST(true),
	PUT(true),
	PATCH(true),
	DELETE(true),
	OPTIONS(false),
	TRACE(false),
	CONNECT(false);

	public static HTTPMethod fromString(String method) {
		return switch (method) {
			case "head", "HEAD" -> HEAD;
			case "get", "GET" -> GET;
			case "post", "POST" -> POST;
			case "put", "PUT" -> PUT;
			case "patch", "PATCH" -> PATCH;
			case "delete", "DELETE" -> DELETE;
			case "options", "OPTIONS" -> OPTIONS;
			case "trace", "TRACE" -> TRACE;
			case "connect", "CONNECT" -> CONNECT;
			default -> throw new IllegalArgumentException("Invalid HTTP method: " + method);
		};
	}

	private final boolean body;

	HTTPMethod(boolean body) {
		this.body = body;
	}

	public boolean body() {
		return body;
	}
}
