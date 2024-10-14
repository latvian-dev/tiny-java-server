package dev.latvian.apps.tinyserver.ws;

import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

public record Frame(FrameInfo info, byte[] payload) {
	static final byte[] EMPTY_PAYLOAD = new byte[0];

	public static Frame simple(Opcode opcode, @Nullable Integer mask, byte[] payload) {
		return new Frame(new FrameInfo(opcode, mask != null, true, false, false, false, mask != null ? mask : 0, payload.length), payload);
	}

	public static Frame text(String text) {
		return simple(Opcode.TEXT, null, text.getBytes(StandardCharsets.UTF_8));
	}

	public static Frame binary(byte[] buffer) {
		return simple(Opcode.BINARY, null, buffer);
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
}
