package dev.latvian.apps.tinyserver.ws;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public record Frame(
	Opcode opcode,
	boolean mask,
	boolean fin,
	boolean rsv1,
	boolean rsv2,
	boolean rsv3,
	byte[] payload
) {
	public static Frame simple(Opcode opcode, boolean mask, byte[] payload) {
		return new Frame(opcode, mask, true, false, false, false, payload);
	}

	public static Frame text(String text) {
		return simple(Opcode.TEXT, false, text.getBytes(StandardCharsets.UTF_8));
	}

	public static Frame binary(byte[] bytes) {
		return simple(Opcode.BINARY, false, bytes);
	}

	public static Frame read(InputStream stream) throws IOException {
		int b1 = stream.read();
		var opcode = Opcode.get(b1 & 0x0F);
		boolean fin = (b1 & 0x80) != 0;
		boolean rsv1 = (b1 & 0x40) != 0;
		boolean rsv2 = (b1 & 0x20) != 0;
		boolean rsv3 = (b1 & 0x10) != 0;

		int b2 = stream.read();
		boolean mask = (b2 & -128) != 0;
		int payloadlength = (byte) (b2 & ~(byte) 128);

		if (payloadlength == 126) {
			var sizebytes = new byte[3];
			sizebytes[1] = (byte) stream.read();
			sizebytes[2] = (byte) stream.read();
			payloadlength = new BigInteger(sizebytes).intValue();
		} else if (payloadlength == 127) {
			byte[] bytes = new byte[8];
			stream.read(bytes);
			payloadlength = (int) new BigInteger(bytes).longValue();
		}

		var payload = new byte[payloadlength];

		if (mask) {
			var maskKey = new byte[4];
			stream.read(maskKey);

			for (int i = 0; i < payloadlength; i++) {
				payload[i] = (byte) (stream.read() ^ maskKey[i % 4]);
			}
		} else {
			stream.read(payload);
		}

		return new Frame(opcode, mask, fin, rsv1, rsv2, rsv3, payload);
	}

	public void write(Random random, OutputStream stream) throws IOException {
		stream.write((fin ? 0x80 : 0)
			| (rsv1 ? 0x40 : 0)
			| (rsv2 ? 0x20 : 0)
			| (rsv3 ? 0x10 : 0)
			| opcode.opcode
		);

		if (payload.length < 126) {
			stream.write((mask ? 0x80 : 0) | payload.length);
		} else if (payload.length < 65536) {
			stream.write((mask ? 0x80 : 0) | 126);
			stream.write(payload.length >> 8);
			stream.write(payload.length);
		} else {
			stream.write((mask ? 0x80 : 0) | 127);
			stream.write(0);
			stream.write(0);
			stream.write(0);
			stream.write(0);
			stream.write(payload.length >> 24);
			stream.write(payload.length >> 16);
			stream.write(payload.length >> 8);
			stream.write(payload.length);
		}

		if (mask) {
			var maskKey = new byte[4];
			random.nextBytes(maskKey);
			stream.write(maskKey);

			for (int i = 0; i < payload.length; i++) {
				stream.write(payload[i] ^ maskKey[i % 4]);
			}
		} else {
			stream.write(payload);
		}
	}

	public Frame appendTo(@Nullable Frame previous) {
		if (previous != null) {
			byte[] newPayload = new byte[previous.payload.length + payload.length];
			System.arraycopy(previous.payload, 0, newPayload, 0, previous.payload.length);
			System.arraycopy(payload, 0, newPayload, previous.payload.length, payload.length);
			return new Frame(previous.opcode, previous.mask, fin, previous.rsv1, previous.rsv2, previous.rsv3, newPayload);
		}

		return this;
	}
}
