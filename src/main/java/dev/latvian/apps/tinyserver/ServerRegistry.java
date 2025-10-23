package dev.latvian.apps.tinyserver;

import dev.latvian.apps.tinyserver.http.HTTPHandler;
import dev.latvian.apps.tinyserver.http.HTTPMethod;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.file.DynamicFileHandler;
import dev.latvian.apps.tinyserver.http.file.FileIndexHandler;
import dev.latvian.apps.tinyserver.http.file.FileResponseHandler;
import dev.latvian.apps.tinyserver.http.file.SingleFileHandler;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.ws.WSEndpointHandler;
import dev.latvian.apps.tinyserver.ws.WSHandler;
import dev.latvian.apps.tinyserver.ws.WSSession;
import dev.latvian.apps.tinyserver.ws.WSSessionFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public interface ServerRegistry<REQ extends HTTPRequest> {
	void http(HTTPMethod method, String path, HTTPHandler<REQ> handler);

	default void get(String path, HTTPHandler<REQ> handler) {
		http(HTTPMethod.GET, path, handler);
	}

	default void post(String path, HTTPHandler<REQ> handler) {
		http(HTTPMethod.POST, path, handler);
	}

	default void acceptPostString(String path, Consumer<String> handler) {
		post(path, req -> {
			handler.accept(req.mainBody().text());
			return HTTPResponse.noContent();
		});
	}

	default void acceptPostTask(String path, Runnable task) {
		post(path, req -> {
			task.run();
			return HTTPResponse.noContent();
		});
	}

	default void put(String path, HTTPHandler<REQ> handler) {
		http(HTTPMethod.PUT, path, handler);
	}

	default void patch(String path, HTTPHandler<REQ> handler) {
		http(HTTPMethod.PATCH, path, handler);
	}

	default void delete(String path, HTTPHandler<REQ> handler) {
		http(HTTPMethod.DELETE, path, handler);
	}

	default void redirect(String path, String redirect) {
		var res = HTTPResponse.redirect(redirect);
		get(path, req -> res);
	}

	default void singleFile(String path, Path file, FileResponseHandler responseHandler) throws IOException {
		get(path, new SingleFileHandler<>(file, Files.probeContentType(file), responseHandler));
	}

	default void dynamicFiles(String path, Path directory, FileResponseHandler responseHandler, boolean autoIndex) {
		var handler = new DynamicFileHandler<REQ>(directory, responseHandler, autoIndex);

		if (autoIndex) {
			get(path, handler);
		}

		get(path + "/<path>", handler);
	}

	default void staticFiles(String path, Path directory, FileResponseHandler responseHandler, boolean autoIndex) throws IOException {
		path = path.endsWith("/") ? path : (path + "/");

		try (var stream = Files.walk(directory)) {
			for (var file : stream.filter(Files::isReadable).toList()) {
				var rpath = directory.relativize(file).toString();

				if (Files.isRegularFile(file)) {
					singleFile(path + rpath, file, responseHandler);
				} else if (autoIndex && Files.isDirectory(file)) {
					get(path + rpath, new FileIndexHandler<>(directory, file, responseHandler));
				}
			}
		}
	}

	default <WSS extends WSSession<REQ>> WSHandler<REQ, WSS> ws(String path, WSSessionFactory<REQ, WSS> factory) {
		var handler = new WSEndpointHandler<>(factory, new ConcurrentHashMap<>());
		get(path, handler);
		return handler;
	}

	default <WSS extends WSSession<REQ>> WSHandler<REQ, WSS> ws(String path) {
		return ws(path, (WSSessionFactory) WSSessionFactory.DEFAULT);
	}
}
