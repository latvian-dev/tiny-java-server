package dev.latvian.apps.tinyhttp.http.response;

import dev.latvian.apps.tinyhttp.HTTPConnection;
import dev.latvian.apps.tinyhttp.NamedString;
import dev.latvian.apps.tinyhttp.content.MimeType;
import dev.latvian.apps.tinyhttp.content.RequestRange;
import dev.latvian.apps.tinyhttp.content.ResponseContent;
import dev.latvian.apps.tinyhttp.http.HTTPRequest;
import dev.latvian.apps.tinyhttp.http.HTTPUpgrade;
import dev.latvian.apps.tinyhttp.http.HeaderConsumer;
import dev.latvian.apps.tinyhttp.util.ByteBufferUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HTTPPayload implements HeaderConsumer {
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).withZone(ZoneId.of("GMT"));

	private final String serverName;
	private final Instant serverTime;
	private HTTPStatus status = HTTPStatus.NO_CONTENT;
	private final List<NamedString> headers = new ArrayList<>();
	private String cacheControl = "";
	private String cors = "";
	private Map<String, String> cookies;
	private ResponseContent body = null;
	private HTTPUpgrade<?> upgrade = null;
	private String encode = null;
	private String acceptRanges = "";

	public HTTPPayload(String serverName, Instant serverTime) {
		this.serverName = serverName;
		this.serverTime = serverTime;
	}

	public void setStatus(HTTPStatus status) {
		this.status = status;
	}

	public HTTPStatus getStatus() {
		return status;
	}

	@Override
	public void addHeader(NamedString header) {
		this.headers.removeIf(h -> h.is(header.name()));
		this.headers.add(header);
	}

	public void setCacheControl(String cacheControl) {
		this.cacheControl = cacheControl;
	}

	public void setCors(String cors) {
		this.cors = cors;
	}

	public void setCookie(String key, String value) {
		if (cookies == null) {
			cookies = new HashMap<>(1);
		}

		cookies.put(key, value);
	}

	public void setBody(ResponseContent body) {
		this.body = body;
	}

	public void setUpgrade(HTTPUpgrade<?> upgrade) {
		this.upgrade = upgrade;
	}

	@Nullable
	public HTTPUpgrade<?> getUpgrade() {
		return upgrade;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	public void setAcceptRanges(String acceptRanges) {
		this.acceptRanges = acceptRanges;
	}

	public void setResponse(HTTPResponse response) {
		setStatus(response.status());
		response.build(this);
	}

	public void write(HTTPRequest req, int keepAliveTimeout, int maxKeepAliveConnections, HTTPConnection<?> connection, boolean writeBody) throws IOException {
		String responseEncoding = null;

		var actualBody = body;

		if (encode != null && actualBody != null) {
			var accepted = req.acceptedEncodings();

			for (var encoding : connection.server().getEncodingMethods()) {
				var name = encoding.name();

				if (!encode.isEmpty() && !encode.equals(name)) {
					continue;
				}

				if (accepted.contains(name)) {
					actualBody = encoding.encode(actualBody);
					responseEncoding = name;
					break;
				}
			}
		}

		var responseHeaders = new ArrayList<NamedString>(headers.size()
			+ (cookies == null ? 0 : cookies.size())
			+ (cacheControl.isEmpty() ? 0 : 1)
			+ (cors.isEmpty() ? 0 : 1)
			+ (responseEncoding == null ? 0 : 1)
		);

		if (serverName != null && !serverName.isEmpty()) {
			responseHeaders.add(NamedString.of("Server", serverName));
		}

		responseHeaders.add(NamedString.of("Date", HTTPPayload.DATE_TIME_FORMATTER.format(serverTime)));

		responseHeaders.addAll(headers);

		if (cookies != null) {
			for (var cookie : cookies.entrySet()) {
				responseHeaders.add(NamedString.of("Set-Cookie", cookie.getKey() + "=" + cookie.getValue()));
			}
		}

		if (!cacheControl.isEmpty()) {
			responseHeaders.add(NamedString.of("Cache-Control", cacheControl));
		}

		if (!cors.isEmpty()) {
			responseHeaders.add(NamedString.of("Access-Control-Allow-Origin", cors));
		}

		if (responseEncoding != null) {
			responseHeaders.add(NamedString.of("Content-Encoding", responseEncoding));
		}

		if (!acceptRanges.isEmpty()) {
			responseHeaders.add(NamedString.of("Accept-Ranges", acceptRanges));
		}

		if (upgrade != null && status == HTTPStatus.SWITCHING_PROTOCOLS) {
			responseHeaders.add(NamedString.of("Connection", "upgrade"));
			responseHeaders.add(NamedString.of("Upgrade", upgrade.protocol()));
		} else if (maxKeepAliveConnections > 0) {
			responseHeaders.add(NamedString.of("Connection", "keep-alive"));
			responseHeaders.add(NamedString.of("Keep-Alive", "timeout=" + keepAliveTimeout + ", max=" + maxKeepAliveConnections));
		} else {
			responseHeaders.add(NamedString.of("Connection", "close"));
		}

		long contentLength = actualBody == null ? -1L : actualBody.length();
		var contentType = actualBody == null ? MimeType.OCTET_STREAM : actualBody.actualType();
		var requestedRanges = List.<RequestRange>of();

		if (status == HTTPStatus.OK && contentLength >= 0L && !acceptRanges.isEmpty()) {
			var range = req.header("Range").asString();

			if (range.startsWith(acceptRanges + "=")) {
				var strArr = range.substring(acceptRanges.length() + 1).split(",");
				requestedRanges = new ArrayList<>(strArr.length);

				for (var str : strArr) {
					requestedRanges.add(RequestRange.parse(str.trim(), contentLength));
				}
			}
		}

		var crlf = ByteBufferUtils.CRLF.duplicate();

		connection.write((!requestedRanges.isEmpty() ? HTTPStatus.PARTIAL_CONTENT : status).responseBuffer().duplicate());
		connection.writeHeaders(responseHeaders);
		responseHeaders.clear();

		if (contentLength >= 0L) {
			if (requestedRanges.isEmpty()) {
				responseHeaders.add(NamedString.of("Content-Length", Long.toUnsignedString(contentLength)));
				responseHeaders.add(NamedString.of("Content-Type", contentType));
			} else if (requestedRanges.size() == 1) {
				var r = requestedRanges.getFirst();
				actualBody = actualBody.withRange(r);
				responseHeaders.add(NamedString.of("Content-Length", Long.toUnsignedString(r.length())));
				responseHeaders.add(NamedString.of("Content-Range", "bytes " + Long.toUnsignedString(r.start()) + "-" + Long.toUnsignedString(r.end()) + "/" + Long.toUnsignedString(contentLength)));
				responseHeaders.add(NamedString.of("Content-Type", contentType));
			} else {
				var dash2 = ByteBufferUtils.DASH2.duplicate();
				var boundary = ByteBufferUtils.BOUNDARY.duplicate();

				var bodyLength = 6L + boundary.limit();

				for (var r : requestedRanges) {
					bodyLength += 10L;
					bodyLength += boundary.limit();
					responseHeaders.add(NamedString.of("Content-Type", contentType));
					responseHeaders.add(NamedString.of("Content-Range", "bytes " + Long.toUnsignedString(r.start()) + "-" + Long.toUnsignedString(r.end()) + "/" + Long.toUnsignedString(contentLength)));
					bodyLength += HTTPConnection.headerSize(responseHeaders);
					bodyLength += r.length();
					responseHeaders.clear();
				}

				responseHeaders.add(NamedString.of("Content-Type", "multipart/byteranges; boundary=" + ByteBufferUtils.BOUNDARY_STRING));
				responseHeaders.add(NamedString.of("Content-Length", Long.toUnsignedString(bodyLength)));
				connection.writeHeaders(responseHeaders);
				responseHeaders.clear();

				if (writeBody) {
					for (var r : requestedRanges) {
						connection.write(crlf.rewind());
						connection.write(dash2.rewind());
						connection.write(boundary.rewind());
						connection.write(crlf.rewind());
						responseHeaders.add(NamedString.of("Content-Type", contentType));
						responseHeaders.add(NamedString.of("Content-Range", "bytes " + Long.toUnsignedString(r.start()) + "-" + Long.toUnsignedString(r.end()) + "/" + Long.toUnsignedString(contentLength)));
						connection.writeHeaders(responseHeaders);
						connection.write(crlf.rewind());
						actualBody.withRange(r).transferTo(connection);
						connection.write(crlf.rewind());
						responseHeaders.clear();
					}

					connection.write(dash2.rewind());
					connection.write(boundary.rewind());
					connection.write(dash2.rewind());
					connection.write(crlf.rewind());
				}

				return;
			}
		}

		connection.writeHeaders(responseHeaders);
		connection.write(crlf);

		if (writeBody && actualBody != null) {
			actualBody.transferTo(connection);
		}
	}
}
