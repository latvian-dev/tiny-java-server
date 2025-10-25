package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.http.HTTPRequest;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.Supplier;

public interface WSHandler<REQ extends HTTPRequest, WSS extends WSSession<REQ>> extends Iterable<WSS> {
	static <REQ extends HTTPRequest, WSS extends WSSession<REQ>> WSHandler<REQ, WSS> empty() {
		return (WSHandler) EmptyWSHandler.INSTANCE;
	}

	Map<UUID, WSS> sessions();

	@Override
	@NotNull
	default Iterator<WSS> iterator() {
		return sessions().values().iterator();
	}

	@Override
	default Spliterator<WSS> spliterator() {
		return sessions().values().spliterator();
	}

	default void broadcast(Frame frame) {
		var s = sessions().values();

		if (!s.isEmpty()) {
			for (var session : s) {
				session.send(frame);
			}
		}
	}

	default void broadcastText(String payload) {
		var s = sessions().values();

		if (!s.isEmpty()) {
			var p = Frame.text(payload);

			for (var session : s) {
				session.send(p);
			}
		}
	}

	default void broadcastText(Supplier<String> payload) {
		var s = sessions().values();

		if (!s.isEmpty()) {
			var p = Frame.text(payload.get());

			for (var session : s) {
				session.send(p);
			}
		}
	}

	default void broadcastBinary(byte[] payload) {
		var s = sessions().values();

		if (!s.isEmpty()) {
			var p = Frame.binary(payload);

			for (var session : s) {
				session.send(p);
			}
		}
	}

	default void broadcastBinary(Supplier<byte[]> payload) {
		var s = sessions().values();

		if (!s.isEmpty()) {
			var p = Frame.binary(payload.get());

			for (var session : s) {
				session.send(p);
			}
		}
	}

	default void broadcastPing(byte[] payload) {
		var s = sessions().values();

		if (!s.isEmpty()) {
			var p = Frame.ping(payload);

			for (var session : s) {
				session.send(p);
			}
		}
	}

	default void broadcastPing(Supplier<byte[]> payload) {
		var s = sessions().values();

		if (!s.isEmpty()) {
			var p = Frame.ping(payload.get());

			for (var session : s) {
				session.send(p);
			}
		}
	}
}
