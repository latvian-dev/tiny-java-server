package dev.latvian.apps.tinyserver.http;

public enum HTTPMethod {
	HEAD("HEAD", false, false),
	GET("GET", false, true),
	POST("POST", true, true),
	PUT("PUT", true, true),
	PATCH("PATCH", true, true),
	DELETE("DELETE", false, true),
	OPTIONS("OPTIONS", false, false),
	TRACE("TRACE", false, true),
	CONNECT("CONNECT", false, false);

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
	private final boolean requestBody;
	private final boolean responseBody;

	HTTPMethod(String name, boolean requestBody, boolean responseBody) {
		this.name = name;
		this.requestBody = requestBody;
		this.responseBody = responseBody;
	}

	public String getName() {
		return name;
	}

	public boolean requestBody() {
		return requestBody;
	}

	public boolean responseBody() {
		return responseBody;
	}
}
