package dev.latvian.apps.tinyserver.test;

import dev.latvian.apps.tinyserver.HTTPServer;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.error.UnauthorizedError;
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
		server.get("/error", TinyServerTest::error);

		server.get("/form", TinyServerTest::form);
		server.get("/form-submit", TinyServerTest::formSubmit);
		server.post("/form-submit", TinyServerTest::formSubmit);

		var testFilesDir = Path.of("src/test/resources");
		server.files("/files", testFilesDir, Duration.ofMinutes(1L), true);
		server.files("/files-no-index", testFilesDir, Duration.ofMinutes(1L), false);

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

	private static HTTPResponse error(TestRequest req) {
		throw new UnauthorizedError(null);
	}

	private static HTTPResponse form(TestRequest req) {
		return HTTPResponse.ok().html("""
			<form action="/form-submit" method="get" accept-charset="utf-8">
			  <label for="fname">First name:</label><br>
			  <input type="text" id="fname" name="fname" value="John"><br>
			  <label for="lname">Last name:</label><br>
			  <input type="text" id="lname" name="lname" value="Doe"><br><br>
			  <input type="submit" value="Submit">
			</form>""");
	}

	private static HTTPResponse formSubmit(TestRequest req) {
		System.out.println("Form data: " + req.formData());
		return HTTPResponse.redirect("/form");
	}
}
