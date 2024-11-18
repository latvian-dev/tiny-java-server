package dev.latvian.apps.tinyserver.http.response;

import dev.latvian.apps.tinyserver.HTTPConnection;
import dev.latvian.apps.tinyserver.OptionalString;
import dev.latvian.apps.tinyserver.content.ByteContent;
import dev.latvian.apps.tinyserver.content.ResponseContent;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.HTTPUpgrade;
import dev.latvian.apps.tinyserver.http.Header;
import dev.latvian.apps.tinyserver.http.response.encoding.ResponseContentEncoding;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
	private static final byte[] HSEP = ": ".getBytes(StandardCharsets.UTF_8);

	private final String serverName;
	private final Instant serverTime;
	private HTTPStatus status = HTTPStatus.NO_CONTENT;
	private final List<Header> headers = new ArrayList<>();
	private String cacheControl = "";
	private Map<String, String> cookies;
	private ResponseContent body = ByteContent.EMPTY;
	private HTTPUpgrade<?> upgrade = null;
	private List<ResponseContentEncoding> encodings;
	private List<Header> responseHeaders = null;

	public HTTPPayload(String serverName, Instant serverTime) {
		this.serverName = serverName;
		this.serverTime = serverTime;
	}

	public void setStatus(HTTPStatus status) {
		this.status = status;
	}

	public void clear() {
		status = HTTPStatus.NO_CONTENT;
		headers.clear();
		cacheControl = "";
		cookies = null;
		body = ByteContent.EMPTY;
		upgrade = null;
		encodings = null;
	}

	public HTTPStatus getStatus() {
		return status;
	}

	public void addHeader(String header, Object value) {
		this.headers.add(new Header(header, String.valueOf(value)));
	}

	public void setHeader(String header, Object value) {
		this.headers.removeIf(h -> h.is(header));
		addHeader(header, value);
	}

	public OptionalString getHeader(String header) {
		for (var h : headers) {
			if (h.is(header)) {
				return h.value();
			}
		}

		return OptionalString.MISSING;
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

	public ResponseContent getBody() {
		return body;
	}

	public void setUpgrade(HTTPUpgrade<?> upgrade) {
		this.upgrade = upgrade;
	}

	@Nullable
	public HTTPUpgrade<?> getUpgrade() {
		return upgrade;
	}

	public void addEncoding(ResponseContentEncoding encoding) {
		if (encodings == null) {
			encodings = new ArrayList<>(1);
		}

		encodings.add(encoding);
	}

	public void setResponse(HTTPResponse response) {
		setStatus(response.status());
		response.build(this);
	}

	public void process(HTTPRequest req, int keepAliveTimeout, int maxKeepAliveConnections) throws IOException {
		String responseEncodings = null;
		boolean hasBodyData = body.hasData();

		if (encodings != null && hasBodyData) {
			var sb = new StringBuilder();

			for (var encoding : encodings) {
				var name = encoding.name();

				if (req.acceptedEncodings().contains(name)) {
					body = encoding.encode(body);

					if (!sb.isEmpty()) {
						sb.append(", ");
					}

					sb.append(name);
				}
			}

			if (!sb.isEmpty()) {
				responseEncodings = sb.toString();
			}
		}

		responseHeaders = new ArrayList<>(headers.size()
			+ (cookies == null ? 0 : cookies.size())
			+ (cacheControl.isEmpty() ? 0 : 1)
			+ (hasBodyData ? 2 : 1)
			+ (responseEncodings == null ? 0 : 1)
		);

		if (serverName != null && !serverName.isEmpty()) {
			responseHeaders.add(new Header("Server", serverName));
		}

		responseHeaders.add(new Header("Date", HTTPPayload.DATE_TIME_FORMATTER.format(serverTime)));

		responseHeaders.addAll(headers);

		if (cookies != null) {
			for (var cookie : cookies.entrySet()) {
				responseHeaders.add(new Header("Set-Cookie", cookie.getKey() + "=" + cookie.getValue()));
			}
		}

		if (!cacheControl.isEmpty()) {
			responseHeaders.add(new Header("Cache-Control", cacheControl));
		}

		if (responseEncodings != null) {
			responseHeaders.add(new Header("Content-Encoding", responseEncodings));
		}

		long contentLength = body.length();
		var contentType = body.type();

		if (contentLength >= 0L) {
			responseHeaders.add(new Header("Content-Length", Long.toUnsignedString(contentLength)));
		}

		if (contentType != null && !contentType.isEmpty()) {
			responseHeaders.add(new Header("Content-Type", contentType));
		}

		if (upgrade != null && status == HTTPStatus.SWITCHING_PROTOCOLS) {
			responseHeaders.add(new Header("Connection", "upgrade"));
			responseHeaders.add(new Header("Upgrade", upgrade.protocol()));
		} else if (maxKeepAliveConnections > 0) {
			responseHeaders.add(new Header("Connection", "keep-alive"));
			responseHeaders.add(new Header("Keep-Alive", "timeout=" + keepAliveTimeout + ", max=" + maxKeepAliveConnections));
		} else {
			responseHeaders.add(new Header("Connection", "close"));
		}
	}

	public void write(HTTPConnection<?> connection, boolean writeBody) throws IOException {
		connection.write(status.responseBuffer().duplicate());

		int size = 2;

		for (var h : responseHeaders) {
			size += h.key().length() + 2 + h.value().asString().length() + 2;
		}

		var buf = ByteBuffer.allocate(size);

		for (var h : responseHeaders) {
			buf.put(h.key().getBytes(StandardCharsets.US_ASCII));
			buf.put(HSEP);
			buf.put(h.value().asString().getBytes(StandardCharsets.US_ASCII));
			buf.put(CRLF);
		}

		buf.put(CRLF);
		buf.flip();

		connection.write(buf);

		if (writeBody && body.hasData()) {
			body.transferTo(connection);
		}
	}
}
