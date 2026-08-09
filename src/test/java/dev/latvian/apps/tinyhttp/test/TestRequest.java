package dev.latvian.apps.tinyhttp.test;

import dev.latvian.apps.tinyhttp.http.HTTPHandler;
import dev.latvian.apps.tinyhttp.http.HTTPRequest;
import dev.latvian.apps.tinyhttp.http.response.HTTPPayload;
import dev.latvian.apps.tinyhttp.http.response.HTTPResponse;
import dev.latvian.apps.tinyhttp.http.response.HTTPStatus;
import dev.latvian.apps.tinyhttp.http.response.error.HTTPError;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class TestRequest extends HTTPRequest {
	@Override
	public HTTPResponse handleResponse(HTTPPayload payload, HTTPResponse response, @Nullable Throwable error) {
		payload.setCacheControl("no-cache, no-store, must-revalidate, max-age=0");

		System.out.println(method() + " /" + fullPath() + " " + payload.getStatus() + ", " + (Instant.now().toEpochMilli() - startTime().toEpochMilli()) + " ms from " + connection());

		if (query("log").asBoolean()) {
			System.out.println("- Headers:");

			for (var h : headers()) {
				System.out.println("  - " + h.name() + ": " + h.value().asString());
			}

			System.out.println("- Query:");

			for (var e : query()) {
				System.out.println("  - " + e.name() + ": " + e.value().asString());
			}

			System.out.println("- Cookies:");

			for (var e : cookies()) {
				System.out.println("  - " + e.name() + ": " + e.value().asString());
			}

			System.out.println("- Accept Encodings: " + acceptedEncodings());
			System.out.println("- Error: " + error);
			System.out.println();
		}

		if (error != null) {
			return (error instanceof HTTPError e ? e.getStatus() : HTTPStatus.INTERNAL_ERROR).text(error.toString());
		}

		return response;
	}

	@Override
	@Nullable
	public HTTPResponse createPreResponse(@Nullable HTTPHandler<?> handler) {
		if (handler == null) {
			return HTTPStatus.NOT_FOUND.text("Page Not Found");
		}

		return super.createPreResponse(handler);
	}
}
