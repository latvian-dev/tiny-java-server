package dev.latvian.apps.tinyserver.http;

import dev.latvian.apps.tinyserver.CompiledPath;
import dev.latvian.apps.tinyserver.HTTPServer;
import dev.latvian.apps.tinyserver.error.InvalidPathException;
import dev.latvian.apps.tinyserver.http.response.HTTPPayload;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPRequest {
	private HTTPServer<?> server;
	private HTTPMethod method;
	private long startTime = 0L;
	private String path = "";
	private String[] pathParts = new String[0];
	private Map<String, String> variables = Map.of();
	private String queryString = "";
	private Map<String, String> query = Map.of();
	private List<Header> headers = List.of();
	private InputStream bodyStream = null;
	private Map<String, String> cookies = null;
	private Map<String, String> formData = null;

	@ApiStatus.Internal
	public void init(HTTPServer<?> server, HTTPMethod method, long startTime, String path, String[] pathParts, CompiledPath compiledPath, List<Header> headers, String queryString, Map<String, String> query, InputStream bodyStream) {
		this.server = server;
		this.method = method;
		this.startTime = startTime;
		this.path = path;
		this.pathParts = pathParts;

		if (compiledPath.variables() > 0) {
			this.variables = new HashMap<>(compiledPath.variables());

			for (var i = 0; i < compiledPath.parts().length; i++) {
				var part = compiledPath.parts()[i];

				if (part.variable() && i < pathParts.length) {
					variables.put(part.name(), pathParts[i]);
				}
			}
		}

		this.headers = headers;
		this.queryString = queryString;
		this.query = query;
		this.bodyStream = bodyStream;
		afterInit();
	}

	public void afterInit() {
	}

	public HTTPServer<?> server() {
		return server;
	}

	public HTTPMethod method() {
		return method;
	}

	public long startTime() {
		return startTime;
	}

	public Map<String, String> variables() {
		return variables;
	}

	public String variable(String name) {
		var s = variables.get(name);

		if (s == null || s.isEmpty()) {
			throw new InvalidPathException("Variable " + name + " not found");
		}

		return s;
	}

	public String queryString() {
		return queryString;
	}

	public Map<String, String> query() {
		return query;
	}

	public List<Header> headers() {
		return Collections.unmodifiableList(headers);
	}

	public String header(String name) {
		for (var header : headers) {
			if (header.key().equalsIgnoreCase(name)) {
				return header.value();
			}
		}

		return "";
	}

	public String path() {
		return path;
	}

	public String fullPath() {
		return path + (queryString.isEmpty() ? "" : "?" + queryString);
	}

	public String[] pathParts() {
		return pathParts;
	}

	public InputStream bodyStream() {
		if (bodyStream == null) {
			return InputStream.nullInputStream();
		}

		return bodyStream;
	}

	public byte[] bodyBytes() throws IOException {
		var h = header("content-length");

		if (h.isEmpty()) {
			return bodyStream().readAllBytes();
		}

		int len = Integer.parseInt(h);
		return bodyStream().readNBytes(len);
	}

	public String body() throws IOException {
		return new String(bodyBytes(), StandardCharsets.UTF_8);
	}

	public Map<String, String> cookies() {
		if (cookies == null) {
			cookies = new HashMap<>(4);

			for (var header : headers) {
				if (header.key().equalsIgnoreCase("Cookie")) {
					for (var part : header.value().split("; ")) {
						var parts = part.split("=", 2);

						if (parts.length == 2) {
							cookies.put(parts[0], parts[1]);
						}
					}
				}
			}
		}

		return cookies;
	}

	@Nullable
	public String cookie(String key) {
		return cookies().get(key);
	}

	public Map<String, String> formData() {
		if (formData == null) {
			formData = new HashMap<>(4);

			try {
				var body = body();

				for (var part : body.split("&")) {
					var parts = part.split("=", 2);

					if (parts.length == 2) {
						formData.put(parts[0], parts[1]);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return formData;
	}

	@Nullable
	public String formData(String key) {
		return formData().get(key);
	}

	public void beforeResponse(HTTPPayload payload, HTTPResponse response) {
	}

	public void afterResponse(HTTPPayload payload, HTTPResponse response) {
	}

	public void handlePayloadError(HTTPPayload payload, Exception error) {
		error.printStackTrace();
	}
}
