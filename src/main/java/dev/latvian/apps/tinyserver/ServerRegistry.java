package dev.latvian.apps.tinyserver;

import dev.latvian.apps.tinyserver.http.HTTPHandler;
import dev.latvian.apps.tinyserver.http.HTTPMethod;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.PathFileHandler;
import dev.latvian.apps.tinyserver.http.RootPathFileHandler;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.ws.WSHandler;
import dev.latvian.apps.tinyserver.ws.WSSession;
import dev.latvian.apps.tinyserver.ws.WSSessionFactory;

import java.nio.file.Path;
import java.time.Duration;
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
			handler.accept(req.body());
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

	default void files(String path, Path directory, Duration cacheDuration, boolean autoInedx) {
		if (autoInedx) {
			get(path, new RootPathFileHandler<>(path, directory));
		}

		get(path + "/<path>", new PathFileHandler<>(path, directory, cacheDuration, autoInedx));
	}

	<WSS extends WSSession<REQ>> WSHandler<REQ, WSS> ws(String path, WSSessionFactory<REQ, WSS> factory);

	default <WSS extends WSSession<REQ>> WSHandler<REQ, WSS> ws(String path) {
		return ws(path, (WSSessionFactory) WSSessionFactory.DEFAULT);
	}
}
