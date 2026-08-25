package dev.latvian.apps.tinyhttp.http;

import dev.latvian.apps.tinyhttp.FormData;
import dev.latvian.apps.tinyhttp.HTTPConnection;
import dev.latvian.apps.tinyhttp.HTTPServer;
import dev.latvian.apps.tinyhttp.NamedString;
import dev.latvian.apps.tinyhttp.OptionalString;
import dev.latvian.apps.tinyhttp.Upload;
import dev.latvian.apps.tinyhttp.content.RequestRange;
import dev.latvian.apps.tinyhttp.error.InvalidPathException;
import dev.latvian.apps.tinyhttp.http.body.Body;
import dev.latvian.apps.tinyhttp.http.body.ChunkedBody;
import dev.latvian.apps.tinyhttp.http.body.EmptyBody;
import dev.latvian.apps.tinyhttp.http.body.ErrorBody;
import dev.latvian.apps.tinyhttp.http.body.MultipartFormDataBody;
import dev.latvian.apps.tinyhttp.http.body.SimpleBody;
import dev.latvian.apps.tinyhttp.http.response.HTTPPayload;
import dev.latvian.apps.tinyhttp.http.response.HTTPResponse;
import dev.latvian.apps.tinyhttp.http.response.error.client.LengthRequiredError;
import dev.latvian.apps.tinyhttp.http.response.error.client.UnprocessableContentError;
import dev.latvian.apps.tinyhttp.http.response.error.server.NotImplementedError;
import dev.latvian.apps.tinyhttp.util.CompiledPath;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
	private List<NamedString> query = List.of();
	private List<NamedString> headers = List.of();
	private List<NamedString> cookies = null;
	private FormData formData = null;
	private Set<String> acceptedEncodings = null;
	private Body body;

	@ApiStatus.Internal
	public final void preInit(HTTPConnection<?> session, Instant startTime, HTTPMethod method) {
		this.connection = session;
		this.startTime = startTime;
		this.method = method;
	}

	@ApiStatus.Internal
	public final void init(String path, String[] pathParts, CompiledPath compiledPath, List<NamedString> headers, String queryString, List<NamedString> query) {
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
		// this.uploads = List.of();

		long len = header("Content-Length").asLong(-1L);

		if (len == 0L) {
			this.body = new EmptyBody(header("Content-Type").asString());
		} else if (len > 0L) {
			var ct = header("Content-Type").asString();
			var ctParts = ct.split(";");
			ctParts[0] = ctParts[0].toLowerCase(Locale.ROOT);

			for (int i = 1; i < ctParts.length; i++) {
				ctParts[i] = ctParts[i].trim();
			}

			if (ctParts[0].startsWith("multipart/form-data")) {
				if (ctParts.length == 2 && ctParts[1].startsWith("boundary=") && ctParts[1].length() > 9) {
					var boundary = ctParts[1].substring(9);
					this.body = new MultipartFormDataBody(connection, len, ct, boundary);
				} else {
					this.body = new ErrorBody("error:multipart_form_data_boundary_missing", () -> new NotImplementedError("Multipart form data boundary is missing"));
				}
			} else if (ctParts[0].startsWith("multipart/")) {
				// https://developer.mozilla.org/en-US/docs/Web/HTTP/Range_requests#multipart_ranges
				this.body = new ErrorBody("error:multipart_byte_data_not_supported", () -> new NotImplementedError("Multipart byte data is currently not supported"));
			} else {
				this.body = new SimpleBody(connection, len, ct);
			}
		} else if (header("Transfer-Encoding").asString().toLowerCase(Locale.ROOT).contains("chunked")) {
			var ct = header("Content-Type").asString();
			var sizeHint = header("X-Content-Length-Hint").asLong(0L);
			this.body = new ChunkedBody(connection, ct, sizeHint);
		} else {
			this.body = new ErrorBody("error:length_required", LengthRequiredError::new);
		}

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

	public List<NamedString> query() {
		return Collections.unmodifiableList(query);
	}

	public boolean hasQuery(String name) {
		for (var ns : query) {
			if (ns.is(name)) {
				return true;
			}
		}

		return false;
	}

	public OptionalString query(String name) {
		for (var ns : query) {
			if (ns.is(name)) {
				return ns.value();
			}
		}

		return OptionalString.MISSING;
	}

	public List<NamedString> headers() {
		return Collections.unmodifiableList(headers);
	}

	public boolean hasHeader(String name) {
		for (var ns : headers) {
			if (ns.is(name)) {
				return true;
			}
		}

		return false;
	}

	public OptionalString header(String name) {
		for (var ns : headers) {
			if (ns.is(name)) {
				return ns.value();
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

	@Nullable
	public Body peekBody() {
		return body;
	}

	public boolean hasBody() {
		return body != null;
	}

	public Body body() {
		if (body == null) {
			throw new UnprocessableContentError("This request has no body");
		}

		return body;
	}

	public List<NamedString> cookies() {
		if (cookies == null) {
			cookies = new ArrayList<>(4);

			for (var header : headers) {
				if (header.is("Cookie")) {
					for (var part : header.value().asString().split("; ")) {
						var parts = part.split("=", 2);

						if (parts.length == 2) {
							cookies.add(NamedString.of(parts[0], parts[1]));
						} else {
							cookies.add(NamedString.empty(parts[0]));
						}
					}
				}
			}
		}

		return cookies;
	}

	public boolean hasCookie(String name) {
		for (var ns : cookies()) {
			if (ns.is(name)) {
				return true;
			}
		}

		return false;
	}

	public OptionalString cookie(String key) {
		for (var ns : cookies()) {
			if (ns.is(key)) {
				return ns.value();
			}
		}

		return OptionalString.MISSING;
	}

	public FormData formData() {
		if (formData == null) {
			if (method.requestBody()) {
				formData = body().formData();
			} else {
				formData = new FormData(query(), List.of());
			}
		}

		return formData;
	}

	public OptionalString formData(String name) {
		return formData().value(name);
	}

	public List<Upload> formUploads() {
		return formData().uploads();
	}

	public List<Upload> formUploads(String name) {
		return formData().uploads(name);
	}

	public Upload formUpload(String name) {
		return formData().upload(name);
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

	public String userAgent() {
		return header("User-Agent").asString();
	}

	public String ip() {
		return header("CF-Connecting-IP").asString();
	}

	public String ipv6() {
		return header("CF-Connecting-IPv6").asString();
	}

	public int ipHash() {
		return connection.server().hash32(ip().getBytes(StandardCharsets.UTF_8));
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

	public String host() {
		var h = header("X-Forwarded-Host").asString();
		return h.isEmpty() ? header("Host").asString() : h;
	}

	public String rootUrl() {
		var h = host();

		if (h.isEmpty() || h.equals("localhost") || h.equals("127.0.0.1")) {
			return "http://localhost";
		}

		var p = header("X-Forwarded-Proto").asString("https");
		return p + "://" + h;
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

	public void afterResponse(HTTPPayload payload, HTTPResponse response, @Nullable HTTPHandler<?> handler, @Nullable Throwable error) {
		if (error != null) {
			error.printStackTrace();
		}
	}

	@Nullable
	public RequestRange range() {
		return null;
	}
}
