package dev.latvian.apps.tinyserver.test;

import dev.latvian.apps.tinyserver.StatusCode;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.ws.WSSession;

public class TestWSSession extends WSSession<HTTPRequest> {
	@Override
	public void onOpen(HTTPRequest req) {
		sendText("Hello from " + id() + "! " + req.variables() + ", " + req.headers());
	}

	@Override
	public void onClose(StatusCode reason, boolean remote) {
		System.out.println("WS " + id() + " Closed: " + reason + ", remote: " + remote);
	}

	@Override
	public void onTextMessage(String message) {
		System.out.println("WS: " + message);
	}
}
