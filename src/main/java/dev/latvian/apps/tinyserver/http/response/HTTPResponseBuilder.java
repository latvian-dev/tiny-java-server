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
import java.util.List;
import java.util.Locale;

public class HTTPResponseBuilder {
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).withZone(ZoneId.of("GMT"));
	private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.UTF_8);

	private HTTPStatus status = HTTPStatus.NO_CONTENT;
	private final List<Header> headers = new ArrayList<>();
	private ResponseContent body = null;
	private WSSession<?> wsSession = null;

	public void setStatus(HTTPStatus status) {
		this.status = status;
	}

	public void setHeader(String header, Object value) {
		this.headers.add(new Header(header, String.valueOf(value)));
	}

	public void setBody(ResponseContent body) {
		this.body = body;
	}

	public void write(OutputStream out, boolean writeBody) throws Exception {
		out.write(status.responseBytes);
		out.write(CRLF);

		for (var header : headers) {
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

	public void setResponse(HTTPResponse response) throws Exception {
		response.build(this);

		if (response instanceof WSResponse res) {
			wsSession = res.session();
		}
	}

	@Nullable
	public WSSession<?> wsSession() {
		return wsSession;
	}
}
