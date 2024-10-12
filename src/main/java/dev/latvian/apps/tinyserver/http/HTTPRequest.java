package dev.latvian.apps.tinyserver.http;

import dev.latvian.apps.tinyserver.CompiledPath;
import dev.latvian.apps.tinyserver.HTTPServer;
import dev.latvian.apps.tinyserver.error.InvalidPathException;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPResponseBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPRequest {
	private HTTPServer<?> server;
	private String path = "";
	private String[] pathParts = new String[0];
	private Map<String, String> variables = Map.of();
	private String queryString = "";
	private Map<String, String> query = Map.of();
	private List<Header> headers = List.of();
	private InputStream bodyStream = null;

	public void init(HTTPServer<?> server, String path, String[] pathParts, CompiledPath compiledPath, List<Header> headers, String queryString, Map<String, String> query, InputStream bodyStream) {
		this.server = server;
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
	}

	public HTTPServer<?> server() {
		return server;
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

	public void handlePayloadError(HTTPResponseBuilder payload, Exception error) {
	}

	public void afterResponse(HTTPResponseBuilder payload, HTTPResponse response) {
	}
}
