package dev.latvian.apps.tinyhttp.util;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public interface ByteBufferUtils {
	ByteBuffer EMPTY_HEAP = ByteBuffer.allocate(0);
	ByteBuffer EMPTY_DIRECT = ByteBuffer.allocateDirect(0);
	ByteBuffer CRLF = directASCII("\r\n");
	ByteBuffer HSEP = directASCII(": ");
	ByteBuffer DASH2 = directASCII("--");
	String BOUNDARY_STRING = (UUID.randomUUID().toString() + UUID.randomUUID()).replace("-", "");
	ByteBuffer BOUNDARY = directASCII(BOUNDARY_STRING);

	static ByteBuffer allocate(int size, boolean direct) {
		if (size == 0) {
			return direct ? EMPTY_DIRECT : EMPTY_HEAP;
		} else {
			return direct ? ByteBuffer.allocateDirect(size) : ByteBuffer.allocate(size);
		}
	}

	static ByteBuffer grow(@Nullable ByteBuffer buffer, int size, boolean direct) {
		if (buffer == null || buffer.capacity() < size) {
			return allocate(size, direct);
		} else {
			return buffer.clear().limit(size);
		}
	}

	static ByteBuffer copy(ByteBuffer buffer) {
		if (!buffer.hasRemaining()) {
			return buffer.isDirect() ? EMPTY_DIRECT : EMPTY_HEAP;
		}

		var newPayload = allocate(buffer.remaining(), buffer.isDirect());
		int pos = buffer.position();
		newPayload.put(buffer);
		buffer.position(pos);
		return newPayload;
	}

	static ByteBuffer join(ByteBuffer a, ByteBuffer b, boolean consume) {
		var newLen = a.remaining() + b.remaining();

		if (newLen == 0) {
			return a.isDirect() && b.isDirect() ? EMPTY_DIRECT : EMPTY_HEAP;
		} else {
			var buffer = allocate(newLen, a.isDirect() && b.isDirect());

			if (consume) {
				buffer.put(a);
				buffer.put(b);
			} else {
				int posA = a.position();
				int posB = b.position();
				buffer.put(a);
				buffer.put(b);
				a.position(posA);
				b.position(posB);
			}

			buffer.rewind();
			return buffer;
		}
	}

	static int bufferSize(long contentSize) {
		int size = (int) Math.min(contentSize, Integer.MAX_VALUE);
		return size >= 1048576 ? 65536 : Math.min(size, 8192);
	}

	static ByteBuffer toDirect(ByteBuffer buffer) {
		if (buffer.isDirect()) {
			return buffer;
		}

		var directBuffer = allocate(buffer.remaining(), true);
		directBuffer.put(buffer);
		return directBuffer;
	}

	static ByteBuffer directASCII(String string) {
		return toDirect(ByteBuffer.wrap(string.getBytes(StandardCharsets.US_ASCII))).rewind().asReadOnlyBuffer();
	}
}
