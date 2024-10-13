package dev.latvian.apps.tinyserver.ws;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record Frame(FrameInfo info, ByteBuffer payload) {
	static final ByteBuffer EMPTY_PAYLOAD = ByteBuffer.allocate(0);

	public static Frame simple(Opcode opcode, @Nullable Integer mask, ByteBuffer payload) {
		return new Frame(new FrameInfo(opcode, mask != null, true, false, false, false, mask != null ? mask : 0, payload.limit()), payload);
	}

	public static Frame simple(Opcode opcode, @Nullable Integer mask, byte[] payload) {
		return simple(opcode, mask, ByteBuffer.wrap(payload));
	}

	public static Frame text(String text) {
		return simple(Opcode.TEXT, null, text.getBytes(StandardCharsets.UTF_8));
	}

	public static Frame binary(byte[] buffer) {
		return simple(Opcode.BINARY, null, buffer);
	}

	public int bytes() {
		return info.bytes() + info().size();
	}

	public Frame appendTo(@Nullable Frame previous) {
		if (previous != null) {
			var newLen = previous.info.size() + info.size();
			ByteBuffer newPayload;

			if (previous.payload == EMPTY_PAYLOAD) {
				newPayload = payload;
			} else if (payload == EMPTY_PAYLOAD) {
				newPayload = previous.payload;
			} else {
				newPayload = ByteBuffer.allocate(newLen);
				newPayload.put(previous.payload);
				newPayload.put(payload);
			}

			return new Frame(new FrameInfo(previous.info.opcode(), previous.info.mask(), info.fin(), previous.info.rsv1(), previous.info.rsv2(), previous.info.rsv3(), previous.info().maskKey(), newLen), newPayload);
		}

		return this;
	}

	public void applyMask() {
		if (info.mask() && !info.maskZero() && info.size() > 0) {
			var payloadBytes = new byte[info.size()];
			payload.get(payloadBytes);
			info.applyMask(payloadBytes);
			payload.flip();
			payload.put(payloadBytes);
			payload.flip();
		}
	}
}
