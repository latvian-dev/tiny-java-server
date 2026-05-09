package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.http.HTTPHandler;
import dev.latvian.apps.tinyhttp.http.HTTPRequest;
import dev.latvian.apps.tinyhttp.http.response.HTTPResponse;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public final class WSEndpointHandler<REQ extends HTTPRequest, WSS extends WSSession<REQ>> implements WSHandler<REQ, WSS>, HTTPHandler<REQ> {
	private static final byte[] WEB_SOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11".getBytes(StandardCharsets.UTF_8);

	private final WSSessionFactory<REQ, WSS> factory;
	private final Map<UUID, WSS> sessions;
	private Map<UUID, WSS> safeSessions;
	private final ReentrantLock lock;

	public WSEndpointHandler(WSSessionFactory<REQ, WSS> factory) {
		this.factory = factory;
		this.sessions = new HashMap<>();
		this.safeSessions = null;
		this.lock = new ReentrantLock();
	}

	@Override
	public HTTPResponse handle(REQ req) throws Exception {
		var session = factory.create(req);
		var key = req.header("Sec-WebSocket-Key").asString().getBytes(StandardCharsets.UTF_8);

		try {
			var data = Base64.getDecoder().decode(key);

			if (data.length == 16) {
				long msb = 0;
				long lsb = 0;

				for (int i = 0; i < 8; i++) {
					msb = (msb << 8) | (data[i] & 0xFF);
				}

				for (int i = 8; i < 16; i++) {
					lsb = (lsb << 8) | (data[i] & 0xFF);
				}

				session.id = new UUID(msb, lsb);
			}
		} catch (Exception ignored) {
		}

		if (session.id == null) {
			session.id = UUID.nameUUIDFromBytes(key);
		}

		var digest = MessageDigest.getInstance("SHA-1");
		digest.update(key);
		digest.update(WEB_SOCKET_GUID);
		var keyAccept = Base64.getEncoder().encodeToString(digest.digest());

		session.handler = this;

		lock.lock();

		try {
			sessions.put(session.id, session);
			safeSessions = null;
		} finally {
			lock.unlock();
		}

		return HTTPResponse.upgrade(session).header("Sec-WebSocket-Accept", keyAccept);
	}

	@Override
	@Unmodifiable
	public Map<UUID, WSS> sessions() {
		var s = safeSessions;

		if (s == null) {
			lock.lock();

			try {
				s = Map.copyOf(sessions);
				safeSessions = s;
			} finally {
				lock.unlock();
			}
		}

		return s;
	}

	void removeSession(UUID id) {
		lock.lock();

		try {
			sessions.remove(id);
			safeSessions = null;
		} finally {
			lock.unlock();
		}
	}
}