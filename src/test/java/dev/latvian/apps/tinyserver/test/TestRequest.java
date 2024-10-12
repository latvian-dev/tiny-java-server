package dev.latvian.apps.tinyserver.test;

import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.response.HTTPPayload;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;
import dev.latvian.apps.tinyserver.http.response.error.HTTPError;
import org.jetbrains.annotations.Nullable;

public class TestRequest extends HTTPRequest {
	@Override
	public HTTPResponse handleResponse(HTTPPayload payload, HTTPResponse response, @Nullable Throwable error) {
		payload.setCacheControl("no-cache, no-store, must-revalidate, max-age=0");

		System.out.println(method() + " /" + fullPath() + " " + payload.getStatus() + ", " + (System.nanoTime() - startTime()) / 10000000L + " ms");
		System.out.println("- Cookies: " + cookies());
		System.out.println("- Headers: " + headers());
		System.out.println("- Query: " + query());
		System.out.println("- Accept Encodings: " + acceptedEncodings());
		System.out.println("- Error: " + error);
		System.out.println();

		if (error != null) {
			return (error instanceof HTTPError e ? e.getStatus() : HTTPStatus.INTERNAL_ERROR).text(error.toString());
		}

		return response;
	}
}
