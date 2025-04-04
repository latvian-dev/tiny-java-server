package dev.latvian.apps.tinyserver.test;

import dev.latvian.apps.tinyserver.HTTPConnection;
import dev.latvian.apps.tinyserver.http.HTTPPathHandler;
import dev.latvian.apps.tinyserver.http.file.FileResponseHandler;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.error.client.UnauthorizedError;
import dev.latvian.apps.tinyserver.ws.WSHandler;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TinyServerTest {
	public static TestServer server;
	public static WSHandler<TestRequest, TestWSSession> wsHandler;

	public static void main(String[] args) throws IOException {
		server = new TestServer();
		server.setServerName("TinyServer Test");
		server.setAddress("127.0.0.1");
		server.setPort(IntStream.range(8080, 8080 + 10));
		server.setDaemon(false);
		server.setKeepAliveTimeout(Duration.ofSeconds(5L));
		server.setMaxKeepAliveConnections(5);

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
		var cache = FileResponseHandler.CACHE_5_MIN;

		server.dynamicFiles("/files/dynamic", testFilesDir, cache, true);
		server.dynamicFiles("/files/dynamic-no-index", testFilesDir, cache, false);
		server.staticFiles("/files/static", testFilesDir, cache, true);
		server.staticFiles("/files/static-no-index", testFilesDir, cache, false);

		wsHandler = server.ws("/console/{console-type}", TestWSSession::new);

		System.out.println("Started server at http://localhost:" + server.start());

		while (server.isRunning()) {
			var sb = new StringBuilder();
			int c;

			while ((c = System.in.read()) != '\n') {
				sb.append((char) c);
			}

			var in = sb.toString();

			if (in.startsWith("/")) {
				simulateRequest(in);
			} else if (in.equals("c")) {
				var uc = server.connections();
				System.out.println("Upgraded Connections: (" + uc.size() + ") " + uc);
			} else if (in.startsWith("+")) {
				wsHandler.broadcastText(in.substring(1));
			}
		}
	}

	private static HTTPResponse homepage(TestRequest req) {
		return HTTPResponse.ok().text("Homepage " + req.startTime() + "\n\n" + req.server().handlers().sorted((a, b) -> a.path().toString().compareToIgnoreCase(b.path().toString())).map(HTTPPathHandler::toString).collect(Collectors.joining("\n")) + "\n\n" + req.server().connections().stream().map(HTTPConnection::toString).collect(Collectors.joining("\n")));
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
		wsHandler.broadcastText(req.mainBody().text());
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
		throw new UnauthorizedError();
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

	public static void simulateRequest(String path) {
		var list = new ArrayList<String>();
		list.add("GET " + path + " HTTP/1.1");
		list.add("Host: localhost");
		list.add("Connection: close");
		list.add("");

		try (var socket = new Socket("localhost", 8080)) {
			socket.setSoTimeout(1000);

			try (var out = socket.getOutputStream()) {
				for (var line : list) {
					out.write((line + "\r\n").getBytes());
				}

				out.flush();

				var sb = new StringBuilder();

				try (var in = socket.getInputStream()) {
					var buffer = new byte[1024];
					var read = 0;

					while ((read = in.read(buffer)) != -1) {
						sb.append(new String(buffer, 0, read));
					}
				}

				System.out.println("Response:\n" + sb.toString().replace("\r\n", "<CRLF>\n"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
