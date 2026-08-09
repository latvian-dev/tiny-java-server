package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.FormData;
import dev.latvian.apps.tinyhttp.http.response.error.client.UnprocessableContentError;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public record EmptyBody(String contentType) implements Body {
	private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
	private static final byte[] EMPTY_BYTES = new byte[0];

	@Override
	public ByteBuffer byteBuffer() {
		return EMPTY_BUFFER;
	}

	@Override
	public void byteBuffer(ByteBuffer to) {
	}

	@Override
	public long transferTo(OutputStream out) {
		return 0L;
	}

	@Override
	public String text() {
		return "";
	}

	@Override
	public byte[] bytes() {
		return EMPTY_BYTES;
	}

	@Override
	public FormData formData() {
		return FormData.EMPTY;
	}

	@Override
	public BufferedImage image() {
		throw new UnprocessableContentError("Not an image");
	}

	@Override
	public String toString() {
		return "empty_body";
	}
}
