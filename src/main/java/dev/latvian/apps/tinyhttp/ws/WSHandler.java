package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.http.HTTPRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Supplier;

public interface WSHandler<REQ extends HTTPRequest, WSS extends WSSession<REQ>> extends Iterable<WSS> {
	@Unmodifiable
	List<WSS> sessions();

	@Override
	@NotNull
	default Iterator<WSS> iterator() {
		return sessions().iterator();
	}

	@Override
	default Spliterator<WSS> spliterator() {
		return sessions().spliterator();
	}

	default void broadcast(Supplier<Frame> frame) {
		var s = sessions();

		if (!s.isEmpty()) {
			var f = frame.get();

			for (var session : s) {
				session.send(f.copy());
			}
		}
	}

	default void broadcastText(String payload) {
		broadcast(() -> Frame.text(payload));
	}

	default void broadcastText(Supplier<String> payload) {
		broadcast(() -> Frame.text(payload.get()));
	}

	default void broadcastBinary(ByteBuffer payload) {
		broadcast(() -> Frame.binary(payload));
	}

	default void broadcastBinary(Supplier<ByteBuffer> payload) {
		broadcast(() -> Frame.binary(payload.get()));
	}

	default void broadcastPing(ByteBuffer payload) {
		broadcast(() -> Frame.ping(payload));
	}

	default void broadcastPing(Supplier<ByteBuffer> payload) {
		broadcast(() -> Frame.ping(payload.get()));
	}
}
