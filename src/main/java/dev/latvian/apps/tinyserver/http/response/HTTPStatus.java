package dev.latvian.apps.tinyserver.http.response;

import dev.latvian.apps.tinyserver.StatusCode;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

public enum HTTPStatus implements HTTPResponse {
	// 1xx - Informational
	CONTINUE(100, "Continue"),
	SWITCHING_PROTOCOLS(101, "Switching Protocols"),

	// 2xx - Successful
	OK(200, "OK"),
	CREATED(201, "Created"),
	ACCEPTED(202, "Accepted"),
	NOT_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
	NO_CONTENT(204, "No Content"),
	RESET_CONTENT(205, "Reset Content"),
	PARTIAL_CONTENT(206, "Partial Content"),

	// 3xx - Redirection
	MULTIPLE_CHOICES(300, "Multiple Choices"),
	MOVED_PERMANENTLY(301, "Moved Permanently"),
	FOUND(302, "Found"),
	SEE_OTHER(303, "See Other"),
	NOT_MODIFIED(304, "Not Modified"),
	USE_PROXY(305, "Use Proxy"),
	TEMPORARY_REDIRECT(307, "Temporary Redirect"),
	PERMANENT_REDIRECT(308, "Permanent Redirect"),

	// 4xx - Client Error
	BAD_REQUEST(400, "Bad Request"),
	UNAUTHORIZED(401, "Unauthorized"),
	PAYMENT_REQUIRED(402, "Payment Required"),
	FORBIDDEN(403, "Forbidden"),
	NOT_FOUND(404, "Not Found"),
	METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
	NOT_ACCEPTABLE(406, "Not Acceptable"),
	PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
	REQUEST_TIMEOUT(408, "Request Timeout"),
	CONFLICT(409, "Conflict"),
	GONE(410, "Gone"),
	LENGTH_REQUIRED(411, "Length Required"),
	PRECONDITION_FAILED(412, "Precondition Failed"),
	CONTENT_TOO_LARGE(413, "Content Too Large"),
	URI_TOO_LONG(414, "URI Too Long"),
	UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
	RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
	EXPECTATION_FAILED(417, "Expectation Failed"),
	MISDIRECTED_REQUEST(421, "Misdirected Request"),
	UNPROCESSABLE_CONTENT(422, "Unprocessable Content"),
	UPGRADE_REQUIRED(426, "Upgrade Required"),

	// 5xx - Server Error
	INTERNAL_ERROR(500, "Internal Server Error"),
	NOT_IMPLEMENTED(501, "Not Implemented"),
	BAD_GATEWAY(502, "Bad Gateway"),
	SERVICE_UNAVAILABLE(503, "Service Unavailable"),
	GATEWAY_TIMEOUT(504, "Gateway Timeout"),
	HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),

	;

	private static final HTTPStatus[] VALUES = values();

	@Nullable
	public static HTTPStatus fromCode(int code) {
		if (code < 100 || code > 599) {
			return null;
		}

		for (var status : VALUES) {
			if (status.statusCode.code() == code) {
				return status;
			}
		}

		return null;
	}

	public final StatusCode statusCode;
	public final byte[] responseBytes;

	HTTPStatus(int code, String message) {
		this.statusCode = new StatusCode(code, message);
		this.responseBytes = ("HTTP/1.1 " + statusCode.code() + " " + statusCode.message()).getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public HTTPStatus status() {
		return this;
	}

	@Override
	public void build(HTTPPayload payload) {
	}

	public boolean informational() {
		return statusCode.code() >= 100 && statusCode.code() < 200;
	}

	public boolean success() {
		return statusCode.code() >= 200 && statusCode.code() < 300;
	}

	public boolean redirect() {
		return statusCode.code() >= 300 && statusCode.code() < 400;
	}

	public boolean clientError() {
		return statusCode.code() >= 400 && statusCode.code() < 500;
	}

	public boolean serverError() {
		return statusCode.code() >= 500 && statusCode.code() < 600;
	}

	public boolean error() {
		return statusCode.code() >= 400;
	}
}
