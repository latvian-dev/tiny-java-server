package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.http.HTTPRequest;
import dev.latvian.apps.tinyhttp.util.ByteBufferUtils;
import dev.latvian.apps.tinyhttp.util.OutputOperations;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record Frame(FrameInfo info, ByteBuffer payload) {
	public static final Frame EMPTY_PING = ping(ByteBufferUtils.EMPTY_DIRECT);

	public static Frame simple(Opcode opcode, @Nullable Integer mask, @Nullable ByteBuffer payload) {
		return new Frame(new FrameInfo(opcode, mask != null, true, false, false, false, mask != null ? mask : 0, payload == null ? 0 : payload.remaining()), payload == null || !payload.hasRemaining() ? ByteBufferUtils.EMPTY_DIRECT : payload.rewind());
	}

	public static Frame text(String text) {
		return simple(Opcode.TEXT, null, text.isEmpty() ? ByteBufferUtils.EMPTY_DIRECT : StandardCharsets.UTF_8.encode(text));
	}

	public static Frame binary(@Nullable ByteBuffer buffer) {
		return simple(Opcode.BINARY, null, buffer);
	}

	public static Frame ping(@Nullable ByteBuffer buffer) {
		return simple(Opcode.PING, null, buffer);
	}

	public static Frame close(int code, String message) {
		var bytes = message.getBytes(StandardCharsets.UTF_8);
		var payload = ByteBufferUtils.allocate(bytes.length + 2, false);
		payload.putShort((short) code);
		payload.put(bytes);
		return simple(Opcode.CLOSING, null, payload);
	}

	public Frame appendTo(@Nullable Frame previous) {
		if (previous == null) {
			return this;
		}

		var newLen = previous.info.size() + info.size();
		var newPayload = ByteBufferUtils.join(previous.payload, payload, true);
		return new Frame(new FrameInfo(previous.info.opcode(), previous.info.mask(), info.fin(), previous.info.rsv1(), previous.info.rsv2(), previous.info.rsv3(), previous.info().maskKey(), newLen), newPayload);
	}

	public <REQ extends HTTPRequest> boolean write(WSSession<REQ> session, OutputOperations operations) {
		if (session.isClosed()) {
			return false;
		}

		try {
			var buf = operations.allocateHeap(info.bytes());
			info.put(buf);
			buf.flip();
			session.connection.write(buf);

			if (info.size() > 0) {
				info.applyMask(payload, 0, payload.remaining());
				session.connection.write(payload);
			}

			return true;
		} catch (Throwable ex) {
			session.handleException(ex);
			return false;
		}
	}

	public ByteBuffer copyPayload() {
		return ByteBufferUtils.copy(payload);
	}

	public Frame copy() {
		return info.size() <= 0 ? this : new Frame(info, copyPayload());
	}
}
