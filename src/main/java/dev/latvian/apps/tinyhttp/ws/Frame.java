package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.http.HTTPRequest;
import dev.latvian.apps.tinyhttp.util.OutputOperations;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record Frame(FrameInfo info, byte[] payload) {
	public static final byte[] EMPTY_PAYLOAD = new byte[0];
	public static final Frame EMPTY_PING = ping(Frame.EMPTY_PAYLOAD);

	public static Frame simple(Opcode opcode, @Nullable Integer mask, byte[] payload) {
		return new Frame(new FrameInfo(opcode, mask != null, true, false, false, false, mask != null ? mask : 0, payload.length), payload);
	}

	public static Frame text(String text) {
		return simple(Opcode.TEXT, null, text.getBytes(StandardCharsets.UTF_8));
	}

	public static Frame binary(byte[] buffer) {
		return simple(Opcode.BINARY, null, buffer);
	}

	public static Frame ping(byte[] buffer) {
		return simple(Opcode.PING, null, buffer);
	}

	public static Frame close(int code, String message) {
		var bytes = message.getBytes(StandardCharsets.UTF_8);
		var payload = new byte[bytes.length + 2];
		payload[0] = (byte) (code & 0xFF);
		payload[1] = (byte) ((code >> 8) & 0xFF);
		System.arraycopy(bytes, 0, payload, 2, bytes.length);
		return simple(Opcode.CLOSING, null, payload);
	}

	public Frame appendTo(@Nullable Frame previous) {
		if (previous != null) {
			var newLen = previous.info.size() + info.size();
			byte[] newPayload;

			if (previous.payload == EMPTY_PAYLOAD) {
				newPayload = payload;
			} else if (payload == EMPTY_PAYLOAD) {
				newPayload = previous.payload;
			} else {
				newPayload = new byte[newLen];
				System.arraycopy(previous.payload, 0, newPayload, 0, previous.info.size());
				System.arraycopy(payload, 0, newPayload, previous.info.size(), info.size());
			}

			return new Frame(new FrameInfo(previous.info.opcode(), previous.info.mask(), info.fin(), previous.info.rsv1(), previous.info.rsv2(), previous.info.rsv3(), previous.info().maskKey(), newLen), newPayload);
		}

		return this;
	}

	public void applyMask() {
		if (info.mask() && !info.maskZero() && info.size() > 0) {
			info.applyMask(payload);
		}
	}

	public <REQ extends HTTPRequest> boolean write(WSSession<REQ> session, OutputOperations operations) {
		if (session.isClosed()) {
			return false;
		}

		try {
			int len = info.bytes();
			var buf = operations.allocate(len);
			info.put(buf);
			buf.flip();
			session.connection.write(buf);

			if (info.size() > 0L) {
				applyMask();
				session.connection.write(ByteBuffer.wrap(payload));
			}

			return true;
		} catch (Throwable ex) {
			session.handleException(ex);
			return false;
		}
	}
}
