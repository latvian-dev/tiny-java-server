package dev.latvian.apps.tinyserver.test;

import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.response.HTTPPayload;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;

public class TestRequest extends HTTPRequest {
	@Override
	public void beforeResponse(HTTPPayload payload, HTTPResponse response) {
		payload.setCacheControl("no-cache, no-store, must-revalidate, max-age=0");
	}

	@Override
	public void afterResponse(HTTPPayload payload, HTTPResponse response) {
		System.out.println(method() + " /" + fullPath() + " " + payload.getStatus() + ", " + (System.currentTimeMillis() - startTime()) + " ms");
		System.out.println("- Cookies: " + cookies());
		System.out.println("- Headers: " + headers());
		System.out.println("- Query: " + query());
		System.out.println("- Accept Encodings: " + acceptedEncodings());
		System.out.println();
	}
}
