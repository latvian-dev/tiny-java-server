package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.http.HTTPRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

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

	default void broadcast(Frame frame) {
		var s = sessions();

		if (!s.isEmpty()) {
			for (var session : s) {
				session.send(frame);
			}
		}
	}

	default void broadcastText(String payload) {
		var s = sessions();

		if (!s.isEmpty()) {
			var p = Frame.text(payload);

			for (var session : s) {
				session.send(p);
			}
		}
	}

	default void broadcastText(Supplier<String> payload) {
		var s = sessions();

		if (!s.isEmpty()) {
			var p = Frame.text(payload.get());

			for (var session : s) {
				session.send(p);
			}
		}
	}

	default void broadcastBinary(byte[] payload) {
		var s = sessions();

		if (!s.isEmpty()) {
			var p = Frame.binary(payload);

			for (var session : s) {
				session.send(p);
			}
		}
	}

	default void broadcastBinary(Supplier<byte[]> payload) {
		var s = sessions();

		if (!s.isEmpty()) {
			var p = Frame.binary(payload.get());

			for (var session : s) {
				session.send(p);
			}
		}
	}

	default void broadcastPing(byte[] payload) {
		var s = sessions();

		if (!s.isEmpty()) {
			var p = Frame.ping(payload);

			for (var session : s) {
				session.send(p);
			}
		}
	}

	default void broadcastPing(Supplier<byte[]> payload) {
		var s = sessions();

		if (!s.isEmpty()) {
			var p = Frame.ping(payload.get());

			for (var session : s) {
				session.send(p);
			}
		}
	}
}
