package dev.latvian.apps.tinyserver.http.response;

import dev.latvian.apps.tinyserver.content.ResponseContent;
import dev.latvian.apps.tinyserver.http.Header;
import dev.latvian.apps.tinyserver.ws.WSResponse;
import dev.latvian.apps.tinyserver.ws.WSSession;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HTTPPayload {
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).withZone(ZoneId.of("GMT"));
	private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.UTF_8);

	private String cacheControl = "";
	private Map<String, String> cookies;
	private HTTPStatus status = HTTPStatus.NO_CONTENT;
	private final List<Header> headers = new ArrayList<>();
	private ResponseContent body = null;
	private WSSession<?> wsSession = null;

	public void setStatus(HTTPStatus status) {
		this.status = status;
	}

	public HTTPStatus getStatus() {
		return status;
	}

	public void addHeader(String header, Object value) {
		this.headers.add(new Header(header, String.valueOf(value)));
	}

	public void setHeader(String header, Object value) {
		this.headers.removeIf(h -> h.key().equalsIgnoreCase(header));
		addHeader(header, value);
	}

	@Nullable
	public String getHeader(String header) {
		for (var h : headers) {
			if (h.key().equalsIgnoreCase(header)) {
				return h.value();
			}
		}

		return null;
	}

	public void setCacheControl(String cacheControl) {
		this.cacheControl = cacheControl;
	}

	public String getCacheControl() {
		return cacheControl;
	}

	public void setCookie(String key, String value) {
		if (cookies == null) {
			cookies = new HashMap<>(1);
		}

		cookies.put(key, value);
	}

	@Nullable
	public String getCookie(String key) {
		return cookies == null ? null : cookies.get(key);
	}

	public void setBody(ResponseContent body) {
		this.body = body;
	}

	@Nullable
	public ResponseContent getBody() {
		return body;
	}

	@Nullable
	public WSSession<?> getWSSession() {
		return wsSession;
	}

	public void setResponse(HTTPResponse response) throws Exception {
		response.build(this);

		if (response instanceof WSResponse res) {
			wsSession = res.session();
		}
	}

	public void write(OutputStream out, boolean writeBody) throws Exception {
		out.write(status.responseBytes);
		out.write(CRLF);

		var actualHeaders = new ArrayList<Header>(headers.size() + (cookies == null ? 0 : cookies.size()) + (cacheControl.isEmpty() ? 0 : 1));
		actualHeaders.addAll(headers);

		if (cookies != null) {
			for (var cookie : cookies.entrySet()) {
				actualHeaders.add(new Header("Set-Cookie", cookie.getKey() + "=" + cookie.getValue()));
			}
		}

		if (!cacheControl.isEmpty()) {
			actualHeaders.add(new Header("Cache-Control", cacheControl));
		}

		for (var header : actualHeaders) {
			out.write((header.key() + ": " + header.value()).getBytes());
			out.write(CRLF);
		}

		if (body != null) {
			long contentLength = body.length();
			var contentType = body.type();

			if (contentLength >= 0L) {
				out.write(("Content-Length: " + Long.toUnsignedString(contentLength)).getBytes());
				out.write(CRLF);
			}

			if (contentType != null && !contentType.isEmpty()) {
				out.write(("Content-Type: " + contentType).getBytes());
				out.write(CRLF);
			}
		}

		out.write(CRLF);

		if (body != null && writeBody) {
			body.write(out);
		}
	}
}
