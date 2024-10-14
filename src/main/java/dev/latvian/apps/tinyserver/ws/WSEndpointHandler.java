package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.http.HTTPHandler;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public record WSEndpointHandler<REQ extends HTTPRequest, WSS extends WSSession<REQ>>(WSSessionFactory<REQ, WSS> factory, Map<UUID, WSS> sessions, boolean daemon) implements WSHandler<REQ, WSS>, HTTPHandler<REQ> {
	private static final byte[] WEB_SOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11".getBytes(StandardCharsets.UTF_8);

	@Override
	public HTTPResponse handle(REQ req) throws Exception {
		var session = factory.create();
		var uuidBase64 = req.header("sec-websocket-key").getBytes(StandardCharsets.UTF_8);
		session.id = UUID.nameUUIDFromBytes(Base64.getDecoder().decode(uuidBase64));

		var digest = MessageDigest.getInstance("SHA-1");
		digest.update(uuidBase64);
		digest.update(WEB_SOCKET_GUID);
		byte[] sha1 = digest.digest();

		session.handler = this;
		sessions.put(session.id, session);

		return HTTPResponse.upgrade(session).header("Sec-WebSocket-Accept", Base64.getEncoder().encodeToString(sha1));
	}
}