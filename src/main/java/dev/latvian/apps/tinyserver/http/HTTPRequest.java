package dev.latvian.apps.tinyserver.http;

import dev.latvian.apps.tinyserver.CompiledPath;
import dev.latvian.apps.tinyserver.HTTPConnection;
import dev.latvian.apps.tinyserver.HTTPServer;
import dev.latvian.apps.tinyserver.OptionalString;
import dev.latvian.apps.tinyserver.error.InvalidPathException;
import dev.latvian.apps.tinyserver.http.response.HTTPPayload;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.error.client.LengthRequiredError;
import dev.latvian.apps.tinyserver.http.response.error.server.NotImplementedError;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HTTPRequest {
	private HTTPConnection<?> connection;
	private Instant startTime;
	private HTTPMethod method;
	private String path = "";
	private String[] pathParts = new String[0];
	private Map<String, OptionalString> variables = Map.of();
	private String queryString = "";
	private Map<String, OptionalString> query = Map.of();
	private List<Header> headers = List.of();
	private Map<String, OptionalString> cookies = null;
	private Map<String, OptionalString> formData = null;
	private Set<String> acceptedEncodings = null;
	private ByteBuffer bodyBuffer = null;
	private List<Body> bodyList = null;

	@ApiStatus.Internal
	public final void preInit(HTTPConnection<?> session, Instant startTime, HTTPMethod method) {
		this.connection = session;
		this.startTime = startTime;
		this.method = method;
	}

	@ApiStatus.Internal
	public final void init(String path, String[] pathParts, CompiledPath compiledPath, List<Header> headers, String queryString, Map<String, OptionalString> query) {
		this.path = path;
		this.pathParts = pathParts;

		if (compiledPath.variables() > 0) {
			this.variables = new HashMap<>(compiledPath.variables());

			for (var i = 0; i < compiledPath.parts().length; i++) {
				var part = compiledPath.parts()[i];

				if (part.variable() && i < pathParts.length) {
					variables.put(part.name(), OptionalString.of(pathParts[i]));
				}
			}
		}

		this.headers = headers;
		this.queryString = queryString;
		this.query = query;
		afterInit();
	}

	public void afterInit() {
	}

	public HTTPConnection<?> connection() {
		return connection;
	}

	public HTTPServer<?> server() {
		return connection.server();
	}

	public HTTPMethod method() {
		return method;
	}

	public Instant startTime() {
		return startTime;
	}

	public Map<String, OptionalString> variables() {
		return variables;
	}

	public OptionalString variable(String name) {
		var s = variables.get(name);

		if (s == null || s.isMissing()) {
			throw new InvalidPathException("Variable " + name + " not found");
		}

		return s;
	}

	public String queryString() {
		return queryString;
	}

	public Map<String, OptionalString> query() {
		return query;
	}

	public OptionalString query(String key) {
		return query.isEmpty() ? OptionalString.MISSING : query.getOrDefault(key, OptionalString.MISSING);
	}

	public List<Header> headers() {
		return Collections.unmodifiableList(headers);
	}

	public OptionalString header(String name) {
		for (var header : headers) {
			if (header.is(name)) {
				return header.value();
			}
		}

		return OptionalString.MISSING;
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

	public ByteBuffer bodyBuffer() throws IOException {
		if (bodyBuffer == null) {
			long len = header("Content-Length").asLong(-1L);

			if (len < 0L) {
				throw new LengthRequiredError();
			}

			bodyBuffer = ByteBuffer.allocate((int) len);
			connection.read(bodyBuffer);
			bodyBuffer.flip();
		}

		return bodyBuffer.position(0);
	}

	public List<Body> bodyList() throws IOException {
		if (bodyList == null) {
			bodyList = new ArrayList<>(1);

			var ct = header("Content-Type").asString();

			if (ct.startsWith("multipart/form-data")) {
				// https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST#multipart_form_submission
				throw new NotImplementedError("Multipart form data is currently not supported!");
			} else if (ct.startsWith("multipart/")) {
				// https://developer.mozilla.org/en-US/docs/Web/HTTP/Range_requests#multipart_ranges
				throw new NotImplementedError("Multipart byte data is currently not supported!");
			} else {
				var body = new Body();
				body.byteBuffer = bodyBuffer();
				body.contentType = ct;
				bodyList.add(body);
			}
		}

		return bodyList;
	}

	public Body mainBody() throws IOException {
		return bodyList().getFirst();
	}

	public Map<String, OptionalString> cookies() {
		if (cookies == null) {
			cookies = new HashMap<>(4);

			for (var header : headers) {
				if (header.is("Cookie")) {
					for (var part : header.value().asString().split("; ")) {
						var parts = part.split("=", 2);

						if (parts.length == 2) {
							cookies.put(parts[0], OptionalString.of(parts[1]));
						}
					}
				}
			}
		}

		return cookies;
	}

	public OptionalString cookie(String key) {
		return cookies().getOrDefault(key, OptionalString.MISSING);
	}

	public Map<String, OptionalString> formData() {
		if (method == HTTPMethod.GET || method == HTTPMethod.HEAD) {
			return query;
		}

		if (formData == null) {
			try {
				formData = mainBody().getPostData();
			} catch (Exception ex) {
				ex.printStackTrace();
				formData = Map.of();
			}
		}

		return formData;
	}

	public Set<String> acceptedEncodings() {
		if (acceptedEncodings == null) {
			acceptedEncodings = new HashSet<>(2);

			for (var header : headers) {
				if (header.is("Accept-Encoding")) {
					Arrays.stream(header.value().asString().split(",")).map(s -> s.trim().split(";")).forEach(s -> acceptedEncodings.add(s[0].trim()));
				}
			}
		}

		return acceptedEncodings;
	}

	public OptionalString formData(String key) {
		return formData().getOrDefault(key, OptionalString.MISSING);
	}

	public String userAgent() {
		return header("User-Agent").asString();
	}

	public String ip() {
		return header("CF-Connecting-IP").asString();
	}

	public String country() {
		return header("CF-IPCountry").asString("XX");
	}

	public String gitHubSignature() {
		return header("X-Hub-Signature").asString();
	}

	public String gitHubEvent() {
		return header("X-GitHub-Event").asString();
	}

	@Nullable
	public HTTPResponse createPreResponse(@Nullable HTTPHandler<?> handler) {
		return null;
	}

	public HTTPResponse handleResponse(HTTPPayload payload, HTTPResponse response, @Nullable Throwable error) {
		if (error != null) {
			error.printStackTrace();
		}

		return response;
	}
}
