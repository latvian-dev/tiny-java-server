package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.HTTPConnection;

import java.io.IOException;
import java.nio.ByteBuffer;

public record FrameInfo(
	Opcode opcode,
	boolean mask,
	boolean fin,
	boolean rsv1,
	boolean rsv2,
	boolean rsv3,
	int maskKey,
	int size
) {
	public static FrameInfo read(HTTPConnection connection) throws IOException {
		int b1 = connection.readByte();

		var opcode = Opcode.get(b1 & 0x0F);
		boolean fin = (b1 & 0x80) != 0;
		boolean rsv1 = (b1 & 0x40) != 0;
		boolean rsv2 = (b1 & 0x20) != 0;
		boolean rsv3 = (b1 & 0x10) != 0;

		int b2 = connection.readByte();
		boolean mask = (b2 & -128) != 0;
		int size = (byte) (b2 & ~(byte) 128);

		if (size == 126) {
			size = connection.readShort() & 0xFFFF;
		} else if (size == 127) {
			size = (int) connection.readLong();
		}

		int maskKey = mask ? connection.readInt() : 0;

		return new FrameInfo(opcode, mask, fin, rsv1, rsv2, rsv3, maskKey, size);
	}

	public void applyMask(byte[] payload) {
		if (payload.length == 0 || maskKey == 0) {
			return;
		}

		byte[] m = new byte[4];
		m[0] = (byte) (maskKey >> 24);
		m[1] = (byte) (maskKey >> 16);
		m[2] = (byte) (maskKey >> 8);
		m[3] = (byte) maskKey;

		for (int i = 0; i < payload.length; i++) {
			payload[i] = (byte) (payload[i] ^ m[i & 3]);
		}
	}

	public int bytes() {
		return 2 + (size < 126 ? 0 : size < 65536 ? 2 : 8) + (mask ? 4 : 0);
	}

	public void put(ByteBuffer buf) {
		buf.put((byte) ((fin ? 0x80 : 0)
			| (rsv1 ? 0x40 : 0)
			| (rsv2 ? 0x20 : 0)
			| (rsv3 ? 0x10 : 0)
			| opcode.opcode
		));

		if (size < 126) {
			buf.put((byte) ((mask ? 0x80 : 0) | size));
		} else if (size < 65536) {
			buf.put((byte) ((mask ? 0x80 : 0) | 126));
			buf.putShort((short) size);
		} else {
			buf.put((byte) ((mask ? 0x80 : 0) | 127));
			buf.putLong(size);
		}

		if (mask) {
			buf.putInt(maskKey);
		}
	}

	public boolean maskZero() {
		return maskKey == 0;
	}
}