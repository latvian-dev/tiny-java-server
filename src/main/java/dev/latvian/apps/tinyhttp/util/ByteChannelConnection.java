package dev.latvian.apps.tinyhttp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;

public class ByteChannelConnection {
	private static final byte R_BYTE = (byte) '\r';
	private static final byte N_BYTE = (byte) '\n';

	protected final ByteChannel channel;
	private final ByteBuffer temp;

	public ByteChannelConnection(ByteChannel channel) {
		this.channel = channel;
		this.temp = ByteBuffer.allocate(8);
	}

	public void read(ByteBuffer buffer) throws IOException {
		do {
			channel.read(buffer);
		}
		while (buffer.hasRemaining());
	}

	public void write(ByteBuffer buffer) throws IOException {
		do {
			channel.write(buffer);
		}
		while (buffer.hasRemaining());
	}

	private ByteBuffer readTemp(int len) throws IOException {
		var buf = temp.clear().limit(len);
		read(buf);
		return buf;
	}

	public void readBytes(byte[] bytes, int off, int len) throws IOException {
		for (var i = 0; i < len; i++) {
			// TODO: Allow to read multiple bytes at once
			bytes[off + i] = readByte();
		}
	}

	public void readBytes(byte[] bytes) throws IOException {
		readBytes(bytes, 0, bytes.length);
	}

	public byte readByte() throws IOException {
		return readTemp(1).get(0);
	}

	public short readShort() throws IOException {
		return readTemp(2).getShort(0);
	}

	public int readInt() throws IOException {
		return readTemp(4).getInt(0);
	}

	public float readFloat() throws IOException {
		return readTemp(4).getFloat(0);
	}

	public long readLong() throws IOException {
		return readTemp(8).getLong(0);
	}

	public double readDouble() throws IOException {
		return readTemp(8).getDouble(0);
	}

	public void readCRLF(OutputStream bytes) throws IOException {
		while (true) {
			byte b = readByte();

			if (b == R_BYTE) {
				byte r = readByte();

				if (r == N_BYTE) {
					break;
				} else {
					bytes.write(R_BYTE);
					bytes.write(r);
				}
			} else {
				bytes.write(b);
			}
		}
	}

	public String readCRLF() throws IOException {
		var bytes = new ByteArrayOutputStream(16);
		readCRLF(bytes);
		return bytes.toString(StandardCharsets.UTF_8);
	}

	public byte[] readCRLFBytes() throws IOException {
		var bytes = new ByteArrayOutputStream(16);
		readCRLF(bytes);
		return bytes.toByteArray();
	}

	public void read(OutputStream out, int len) throws IOException {
		for (var i = 0; i < len; i++) {
			// TODO: Allow to read multiple bytes at once
			out.write(readByte());
		}
	}
}
