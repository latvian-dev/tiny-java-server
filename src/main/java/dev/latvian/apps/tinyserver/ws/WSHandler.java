package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.http.HTTPRequest;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public interface WSHandler<REQ extends HTTPRequest, WSS extends WSSession<REQ>> {
	static <REQ extends HTTPRequest, WSS extends WSSession<REQ>> WSHandler<REQ, WSS> empty() {
		return (WSHandler) EmptyWSHandler.INSTANCE;
	}

	Map<UUID, WSS> sessions();

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
}
