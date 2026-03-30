package dev.latvian.apps.tinyserver.http;

public enum HTTPMethod {
	HEAD("HEAD", false),
	GET("GET", true),
	POST("POST", true),
	PUT("PUT", true),
	PATCH("PATCH", true),
	DELETE("DELETE", true),
	OPTIONS("OPTIONS", false),
	TRACE("TRACE", false),
	CONNECT("CONNECT", false);

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

	public final String name;
	private final boolean body;

	HTTPMethod(String name, boolean body) {
		this.name = name;
		this.body = body;
	}

	public String getName() {
		return name;
	}

	public boolean body() {
		return body;
	}
}
