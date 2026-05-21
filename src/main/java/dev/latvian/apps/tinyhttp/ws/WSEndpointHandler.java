package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.http.HTTPHandler;
import dev.latvian.apps.tinyhttp.http.HTTPRequest;
import dev.latvian.apps.tinyhttp.http.response.HTTPResponse;
import dev.latvian.apps.tinyhttp.http.response.error.client.BadRequestError;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public final class WSEndpointHandler<REQ extends HTTPRequest, WSS extends WSSession<REQ>> implements WSHandler<REQ, WSS>, HTTPHandler<REQ> {
	private static final byte[] WEB_SOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11".getBytes(StandardCharsets.UTF_8);

	private final WSSessionFactory<REQ, WSS> factory;
	private final List<WSS> sessions;
	private List<WSS> safeSessions;
	private final ReentrantLock lock;

	public WSEndpointHandler(WSSessionFactory<REQ, WSS> factory) {
		this.factory = factory;
		this.sessions = new LinkedList<>();
		this.safeSessions = null;
		this.lock = new ReentrantLock();
	}

	@Override
	public HTTPResponse handle(REQ req) throws Exception {
		var session = factory.create(req);
		session.key = req.header("Sec-WebSocket-Key").asString();

		if (session.key.isEmpty()) {
			throw new BadRequestError("Invalid Sec-WebSocket-Key header");
		}

		var digest = MessageDigest.getInstance("SHA-1");
		digest.update(session.key.getBytes(StandardCharsets.UTF_8));
		digest.update(WEB_SOCKET_GUID);
		var keyAccept = Base64.getEncoder().encodeToString(digest.digest());

		session.handler = this;

		lock.lock();

		try {
			sessions.add(session);
			safeSessions = null;
		} finally {
			lock.unlock();
		}

		return HTTPResponse.upgrade(session).header("Sec-WebSocket-Accept", keyAccept);
	}

	@Override
	@Unmodifiable
	public List<WSS> sessions() {
		var s = safeSessions;

		if (s == null) {
			lock.lock();

			try {
				s = List.copyOf(sessions);
				safeSessions = s;
			} finally {
				lock.unlock();
			}
		}

		return s;
	}

	void removeSession(WSSession<REQ> session) {
		lock.lock();

		try {
			//noinspection SuspiciousMethodCalls
			sessions.remove(session);
			safeSessions = null;
		} finally {
			lock.unlock();
		}
	}
}