package dev.latvian.apps.tinyhttp.util;

import dev.latvian.apps.tinyhttp.NamedString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

public class ByteChannelConnection {
	private static final byte R_BYTE = (byte) '\r';
	private static final byte N_BYTE = (byte) '\n';

	protected final ByteChannel channel;
	private ByteBuffer temp;
	private byte[] tempBytes;

	public ByteChannelConnection(ByteChannel channel) {
		this.channel = channel;
		this.temp = ByteBufferUtils.allocate(8, false);
		this.tempBytes = new byte[8];
	}

	public ByteChannel getChannel() {
		return channel;
	}

	private byte[] tempBytes(int len) {
		if (len > tempBytes.length) {
			tempBytes = new byte[len];
		}

		return tempBytes;
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

	public void write(Path path) throws IOException {
		write0(path, 0L, Files.size(path), false);
	}

	public void write(Path path, long start, long length) throws IOException {
		write0(path, start, length, length < Files.size(path));
	}

	public void write0(Path path, long start, long length, boolean truncate) throws IOException {
		try (var fileChannel = truncate ? Files.newByteChannel(path, StandardOpenOption.READ).truncate(start + length) : Files.newByteChannel(path, StandardOpenOption.READ)) {
			fileChannel.position(start);
			var buf = ByteBufferUtils.allocate(ByteBufferUtils.bufferSize(length), true);

			while (fileChannel.read(buf) != -1) {
				buf.flip();
				write(buf);
				buf.clear();
			}
		}
	}

	public static int headerSize(Collection<NamedString> list) {
		if (list.isEmpty()) {
			return 0;
		}

		int size = 0;

		for (var h : list) {
			size += h.name().length() + 2 + h.value().asString().length() + 2;
		}

		return size;
	}

	public void writeHeaders(Collection<NamedString> list) throws IOException {
		if (list.isEmpty()) {
			return;
		}

		var out = new ByteArrayOutputStream(headerSize(list));

		for (var h : list) {
			out.write(h.name().getBytes(StandardCharsets.US_ASCII));
			out.write(':');
			out.write(' ');
			out.write(h.value().asString().getBytes(StandardCharsets.US_ASCII));
			out.write('\r');
			out.write('\n');
		}

		write(ByteBuffer.wrap(out.toByteArray()));
	}

	private ByteBuffer readTemp(int len) throws IOException {
		temp = ByteBufferUtils.grow(temp, len, false);
		read(temp);
		return temp;
	}

	public void readBytes(byte[] bytes, int off, int len) throws IOException {
		while (len > 0) {
			int count = ByteBufferUtils.bufferSize(len);
			var temp = readTemp(count);
			temp.get(0, bytes, off, count);
			off += count;
			len -= count;
		}
	}

	public void readBytes(byte[] bytes) throws IOException {
		readBytes(bytes, 0, bytes.length);
	}

	public void read(int len, OutputStream out) throws IOException {
		while (len > 0) {
			int count = ByteBufferUtils.bufferSize(len);
			var bytes = tempBytes(count);
			var temp = readTemp(count);
			temp.get(0, bytes, 0, count);
			out.write(bytes, 0, count);
			len -= count;
		}
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
}
