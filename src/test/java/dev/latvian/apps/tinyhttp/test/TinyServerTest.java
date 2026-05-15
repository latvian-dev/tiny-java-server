package dev.latvian.apps.tinyhttp.test;

import dev.latvian.apps.tinyhttp.HTTPConnection;
import dev.latvian.apps.tinyhttp.http.file.FileResponseHandler;
import dev.latvian.apps.tinyhttp.http.response.HTTPResponse;
import dev.latvian.apps.tinyhttp.http.response.error.client.UnauthorizedError;
import dev.latvian.apps.tinyhttp.util.HTTPPathHandler;
import dev.latvian.apps.tinyhttp.ws.WSHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class TinyServerTest {
	private static final HttpClient HTTP_CLIENT = HttpClient
		.newBuilder()
		.version(HttpClient.Version.HTTP_1_1)
		.executor(Executors.newVirtualThreadPerTaskExecutor())
		.build();

	public static TestServer server;
	public static WSHandler<TestRequest, TestWSSession> wsHandler;

	public static void main(String[] args) throws Exception {
		server = new TestServer();
		server.setServerName("Tiny HTTP Server Test");
		server.setAddress("127.0.0.1");
		server.setPortRange(8080, 8090);
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
		server.post("/upload", TinyServerTest::upload);

		var testFilesDir = Path.of("src/test/resources");
		var cache = FileResponseHandler.CACHE_5_MIN;

		server.dynamicFiles("/files/dynamic", testFilesDir, cache, true);
		server.dynamicFiles("/files/dynamic-no-index", testFilesDir, cache, false);
		server.staticFiles("/files/static", testFilesDir, cache, true);
		server.staticFiles("/files/static-no-index", testFilesDir, cache, false);

		wsHandler = server.ws("/console/{console-type}", TestWSSession::new);

		server.start();

		System.out.println("Started server at http://localhost:" + server.getBoundPort());

		while (server.isRunning()) {
			var sb = new StringBuilder();
			int c;

			while ((c = System.in.read()) != '\n') {
				sb.append((char) c);
			}

			var in = sb.toString();

			if (in.startsWith("/")) {
				simulateRequest(in);
			} else if (in.startsWith("!/")) {
				simulateChunkedUpload(in.substring(1));
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

	private static HTTPResponse upload(TestRequest req) throws Exception {
		var bytes = new byte[128 * 1024];
		new Random(1L).nextBytes(bytes);
		return HTTPResponse.ok().text("" + Arrays.equals(bytes, req.mainBody().bytes()));
	}

	public static void simulateRequest(String path) {
		var request = HttpRequest.newBuilder(URI.create("http://localhost:8080/" + path)).GET().build();

		try {
			var response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println("Response " + response.statusCode() + ":\n" + String.valueOf(response.body()).replace("\r\n", "<CRLF>\n"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void simulateChunkedUpload(String path) {
		var bytes = new byte[128 * 1024];
		new Random(1L).nextBytes(bytes);
		var body = HttpRequest.BodyPublishers.ofInputStream(() -> new ByteArrayInputStream(bytes));
		var request = HttpRequest.newBuilder(URI.create("http://localhost:8080/" + path)).POST(body).build();

		try {
			var response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println("Response " + response.statusCode() + ":\n" + String.valueOf(response.body()).replace("\r\n", "<CRLF>\n"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
