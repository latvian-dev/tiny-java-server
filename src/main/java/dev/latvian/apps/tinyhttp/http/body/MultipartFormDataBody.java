package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.FormData;
import dev.latvian.apps.tinyhttp.NamedString;
import dev.latvian.apps.tinyhttp.Upload;
import dev.latvian.apps.tinyhttp.util.ByteChannelConnection;
import dev.latvian.apps.tinyhttp.util.StringParser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public final class MultipartFormDataBody extends SimpleBody {
	private final String boundary;
	private FormData formData;

	public MultipartFormDataBody(ByteChannelConnection connection, int contentLength, String contentType, String boundary) {
		super(connection, contentLength, contentType);
		this.boundary = boundary;
	}

	@Override
	public FormData formData() {
		if (formData == null) {
			var bytes = bytes();

			var values = new ArrayList<NamedString>();
			var uploads = new ArrayList<Upload>();
			// https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Disposition#html_posting_multipartform-data_content_type
			var text = new String(bytes, StandardCharsets.ISO_8859_1);

			if (text.startsWith("--" + boundary + "\r\n") && text.endsWith("\r\n--" + boundary + "--\r\n")) {
				text = text.substring(boundary.length() + 4, text.length() - boundary.length() - 8);
				var blocks = text.split("\r\n--" + boundary + "\r\n");

				for (var block : blocks) {
					var blockParts = block.split("\r\n\r\n", 2);

					if (blockParts.length == 2) {
						var headers = new ArrayList<NamedString>(2);
						var name = "";
						String fileName = null;
						var contentType = "";

						for (var line : blockParts[0].split("\r\n")) {
							var reader = new StringParser(line);
							reader.skipWhitespace();
							var header = reader.until(StringParser.CharPredicate.COL);
							reader.next();
							reader.skipWhitespace();
							var value = reader.remaining();
							headers.add(NamedString.of(header, value));

							if (header.equalsIgnoreCase("Content-Disposition")) {
								var parameters = reader.parameters();

								for (var parameter : parameters) {
									if (parameter.is("name")) {
										name = parameter.value().asString();
									} else if (parameter.is("filename")) {
										fileName = parameter.value().asString();
									}
								}
							} else if (header.equalsIgnoreCase("Content-Type")) {
								contentType = value;
							}
						}

						if (fileName != null) {
							if (!fileName.isEmpty()) {
								var content = blockParts[1];
								var blockBytes = content.getBytes(StandardCharsets.ISO_8859_1);
								uploads.add(new Upload(name, fileName, headers, new UploadBody(blockBytes, contentType)));
							}
						} else {
							values.add(NamedString.of(name, blockParts[1]));
						}
					}
				}
			}

			formData = values.isEmpty() && uploads.isEmpty() ? FormData.EMPTY : new FormData(values, uploads);
		}

		return formData;
	}

	@Override
	public String toString() {
		return "multipart_body; " + contentLength() + " bytes";
	}
}
