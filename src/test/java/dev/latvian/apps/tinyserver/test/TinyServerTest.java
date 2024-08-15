package dev.latvian.apps.tinyserver.test;

import dev.latvian.apps.tinyserver.HTTPServer;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.ws.WSHandler;
import dev.latvian.apps.tinyserver.ws.WSSession;

import java.io.IOException;

public class TinyServerTest {
	public static HTTPServer<HTTPRequest> server;
	public static WSHandler<HTTPRequest, WSSession<HTTPRequest>> wsHandler;

	public static void main(String[] args) {
		server = new HTTPServer<>(HTTPRequest::new);
		server.setServerName("TinyServer Test");
		server.setAddress("127.0.0.1");
		server.setPort(8080);
		server.setMaxPortShift(10);
		server.setDaemon(false);

		server.get("/", TinyServerTest::homepage);
		server.get("/test", TinyServerTest::test);
		server.get("/variable/{test}", TinyServerTest::variable);
		server.get("/varpath/<test>", TinyServerTest::varpath);
		server.get("/redirect", TinyServerTest::redirect);
		server.post("/console", TinyServerTest::console);
		server.get("/stop", TinyServerTest::stop);
		wsHandler = server.ws("/console/{console-type}", TestWSSession::new);

		System.out.println("Started server at https://localhost:" + server.start());
	}

	private static HTTPResponse homepage(HTTPRequest req) {
		return HTTPResponse.ok().text("Homepage");
	}

	private static HTTPResponse test(HTTPRequest req) {
		return HTTPResponse.ok().text("Test");
	}

	private static HTTPResponse variable(HTTPRequest req) {
		return HTTPResponse.ok().text("Test: " + req.variables().get("test")).header("X-ABC", "Def");
	}

	private static HTTPResponse varpath(HTTPRequest req) {
		return HTTPResponse.ok().text("Test: " + req.variables().get("test"));
	}

	private static HTTPResponse console(HTTPRequest req) throws IOException {
		wsHandler.broadcastText(req.body());
		return HTTPResponse.noContent();
	}

	private static HTTPResponse redirect(HTTPRequest req) {
		return HTTPResponse.redirect("/");
	}

	private static HTTPResponse stop(HTTPRequest req) {
		server.stop();
		return HTTPResponse.noContent();
	}
}
