package dev.latvian.apps.tinyserver.http;

import dev.latvian.apps.tinyserver.CompiledPath;
import dev.latvian.apps.tinyserver.HTTPServer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HTTPRequest {
	private HTTPServer<?> server;
	private String[] path = new String[0];
	private Map<String, String> variables = Map.of();
	private Map<String, String> query = Map.of();
	private Map<String, String> headers = Map.of();
	private InputStream bodyStream = null;

	public void init(HTTPServer<?> server, String[] path, CompiledPath compiledPath, Map<String, String> headers, Map<String, String> query, InputStream bodyStream) {
		this.server = server;
		this.path = path;

		if (compiledPath.variables() > 0) {
			this.variables = new HashMap<>(compiledPath.variables());

			for (var i = 0; i < compiledPath.parts().length; i++) {
				var part = compiledPath.parts()[i];

				if (part.variable() && i < path.length) {
					variables.put(part.name(), path[i]);
				}
			}
		}

		this.headers = headers;
		this.query = query;
		this.bodyStream = bodyStream;
	}

	public HTTPServer<?> server() {
		return server;
	}

	public Map<String, String> variables() {
		return variables;
	}

	public Map<String, String> query() {
		return query;
	}

	public Map<String, String> headers() {
		return Collections.unmodifiableMap(headers);
	}

	public String header(String name) {
		return headers.getOrDefault(name.toLowerCase(), "");
	}

	public String[] path() {
		return path;
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
}
