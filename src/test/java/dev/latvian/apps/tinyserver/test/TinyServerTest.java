package dev.latvian.apps.tinyserver.test;

import dev.latvian.apps.tinyserver.HTTPServer;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.ws.WSHandler;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

public class TinyServerTest {
	public static HTTPServer<TestRequest> server;
	public static WSHandler<TestRequest, TestWSSession> wsHandler;

	public static void main(String[] args) {
		server = new HTTPServer<>(TestRequest::new);
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
		server.files("/files", Path.of("src/test/resources"), Duration.ofMinutes(1L), true);
		server.files("/files-no-index", Path.of("src/test/resources"), Duration.ofMinutes(1L), false);

		wsHandler = server.ws("/console/{console-type}", TestWSSession::new);

		System.out.println("Started server at https://localhost:" + server.start());
	}

	private static HTTPResponse homepage(TestRequest req) {
		return HTTPResponse.ok().text("Homepage");
	}

	private static HTTPResponse test(TestRequest req) {
		return HTTPResponse.ok().text("Test");
	}

	private static HTTPResponse variable(TestRequest req) {
		return HTTPResponse.ok().text("Test: " + req.variable("test")).header("X-ABC", "Def");
	}

	private static HTTPResponse varpath(TestRequest req) {
		return HTTPResponse.ok().text("Test: " + req.variable("test"));
	}

	private static HTTPResponse console(TestRequest req) throws IOException {
		wsHandler.broadcastText(req.body());
		return HTTPResponse.noContent();
	}

	private static HTTPResponse redirect(TestRequest req) {
		return HTTPResponse.redirect("/");
	}

	private static HTTPResponse stop(TestRequest req) {
		server.stop();
		return HTTPResponse.noContent();
	}
}
