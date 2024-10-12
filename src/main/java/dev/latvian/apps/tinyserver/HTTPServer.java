package dev.latvian.apps.tinyserver;

import dev.latvian.apps.tinyserver.error.BindFailedException;
import dev.latvian.apps.tinyserver.error.InvalidPathException;
import dev.latvian.apps.tinyserver.http.HTTPHandler;
import dev.latvian.apps.tinyserver.http.HTTPMethod;
import dev.latvian.apps.tinyserver.http.HTTPPathHandler;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.Header;
import dev.latvian.apps.tinyserver.http.response.HTTPResponseBuilder;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;
import dev.latvian.apps.tinyserver.ws.WSEndpointHandler;
import dev.latvian.apps.tinyserver.ws.WSHandler;
import dev.latvian.apps.tinyserver.ws.WSSession;
import dev.latvian.apps.tinyserver.ws.WSSessionFactory;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HTTPServer<REQ extends HTTPRequest> implements Runnable, ServerRegistry<REQ> {
	private final Supplier<REQ> requestFactory;
	private final Map<HTTPMethod, HandlerList<REQ>> handlers;
	private final Map<HTTPMethod, HTTPPathHandler<REQ>> rootHandlers;
	private String serverName;
	private ServerSocket serverSocket;
	private String address;
	private int port = 8080;
	private int maxPortShift = 0;
	private boolean daemon = false;
	private int bufferSize = 8192;

	public HTTPServer(Supplier<REQ> requestFactory) {
		this.requestFactory = requestFactory;
		this.handlers = new EnumMap<>(HTTPMethod.class);
		this.rootHandlers = new EnumMap<>(HTTPMethod.class);
		this.serverName = "dev.latvian.apps:tiny-java-server";
	}

	public void setServerName(String name) {
		this.serverName = name;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setMaxPortShift(int maxPortShift) {
		this.maxPortShift = Math.max(maxPortShift, 0);
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public int start() {
		if (serverSocket != null) {
			throw new IllegalStateException("Server is already running");
		}

		for (int i = port; i <= port + maxPortShift; i++) {
			try {
				serverSocket = new ServerSocket(i, 0, address == null ? null : InetAddress.getByName(address));
				break;
			} catch (Exception ignore) {
			}
		}

		if (serverSocket == null) {
			throw new BindFailedException(port, port + maxPortShift);
		}

		var thread = new Thread(this);
		thread.setDaemon(daemon);
		thread.start();
		return serverSocket.getLocalPort();
	}

	public void stop() {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException ignore) {
			}
		}

		serverSocket = null;
	}

	@Override
	public void http(HTTPMethod method, String path, HTTPHandler<REQ> handler) {
		var compiledPath = CompiledPath.compile(path);
		var pathHandler = new HTTPPathHandler<>(method, compiledPath, handler);

		if (compiledPath == CompiledPath.EMPTY) {
			rootHandlers.put(method, pathHandler);
		} else {
			var hl = handlers.computeIfAbsent(method, HandlerList::new);

			if (compiledPath.variables() > 0) {
				hl.dynamicHandlers().add(pathHandler);
			} else {
				hl.staticHandlers().put(Arrays.stream(compiledPath.parts()).map(CompiledPath.Part::name).collect(Collectors.joining("/")), pathHandler);
			}
		}
	}

	@Override
	public <WSS extends WSSession<REQ>> WSHandler<REQ, WSS> ws(String path, WSSessionFactory<REQ, WSS> factory) {
		var handler = new WSEndpointHandler<>(factory, new ConcurrentHashMap<>(), daemon);
		get(path, handler);
		return handler;
	}

	@Override
	public void run() {
		try (var s = serverSocket) {
			while (serverSocket != null) {
				var socket = s.accept();
				Thread.startVirtualThread(() -> handleClient(socket));
			}
		} catch (Exception ignore) {
		}
	}

	private String readLine(InputStream in) throws IOException {
		var sb = new StringBuilder();
		int b;

		while ((b = in.read()) != -1) {
			if (b == '\n') {
				break;
			}

			if (b != '\r') {
				sb.append((char) b);
			}
		}

		return sb.toString();
	}

	private void handleClient(Socket socket) {
		InputStream in = null;
		OutputStream out = null;
		WSSession<REQ> upgradedToWebSocket = null;

		try {
			in = new BufferedInputStream(socket.getInputStream(), bufferSize);
			var firstLineStr = readLine(in);

			if (!firstLineStr.toLowerCase().endsWith(" http/1.1")) {
				return;
			}

			firstLineStr = firstLineStr.substring(0, firstLineStr.length() - 9).trim();
			var firstLine = firstLineStr.split(" ", 2);

			var method = firstLine.length == 2 ? HTTPMethod.fromString(firstLine[0]) : null;
			boolean writeBody = method != null && method.body();

			if (method == HTTPMethod.HEAD) {
				method = HTTPMethod.GET;
			}

			if (method != null) {
				var path = firstLine[1];

				while (path.startsWith("/")) {
					path = path.substring(1);
				}

				var queryString = "";
				var qi = path.indexOf('?');

				if (qi != -1) {
					queryString = path.substring(qi + 1);
					path = path.substring(0, qi);
				}

				while (path.endsWith("/")) {
					path = path.substring(0, path.length() - 1);
				}

				Map<String, String> query = queryString.isEmpty() ? Map.of() : new HashMap<>();

				if (!queryString.isEmpty()) {
					for (var part : queryString.split("&")) {
						var parts = part.split("=", 2);

						if (parts.length == 2) {
							query.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
						} else {
							query.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), "");
						}
					}
				}

				var headers = new ArrayList<Header>();

				while (true) {
					var line = readLine(in);

					if (line.isBlank()) {
						break;
					}

					var parts = line.split(":", 2);

					if (parts.length == 2) {
						headers.add(new Header(parts[0].trim(), parts[1].trim()));
					}
				}

				var req = requestFactory.get();

				if (method == HTTPMethod.OPTIONS) {
					var allowed = new HashSet<HTTPMethod>();
					allowed.add(HTTPMethod.OPTIONS);

					if (path.isEmpty()) {
						allowed.addAll(rootHandlers.keySet());
					} else if (path.equals("*")) {
						allowed.addAll(rootHandlers.keySet());

						for (var handler : handlers.entrySet()) {
							allowed.add(handler.getKey());
						}
					} else {
						var pathParts = path.split("/");

						for (int i = 0; i < pathParts.length; i++) {
							try {
								if (pathParts[i].indexOf('%') != -1) {
									pathParts[i] = URLDecoder.decode(pathParts[i], StandardCharsets.UTF_8);
								}
							} catch (Exception ex) {
								throw new InvalidPathException(ex.getMessage());
							}
						}

						for (var handler : handlers.entrySet()) {
							if (handler.getValue().staticHandlers().containsKey(path)) {
								allowed.add(handler.getKey());
							}

							for (var dynamicHandler : handler.getValue().dynamicHandlers()) {
								if (dynamicHandler.path().matches(pathParts) != null) {
									allowed.add(handler.getKey());
								}
							}
						}
					}

					if (allowed.contains(HTTPMethod.GET)) {
						allowed.add(HTTPMethod.HEAD);
					}

					var builder = createBuilder(req, null);
					builder.setStatus(HTTPStatus.NO_CONTENT);
					builder.setHeader("Allow", allowed.stream().map(HTTPMethod::name).collect(Collectors.joining(",")));
					out = new BufferedOutputStream(socket.getOutputStream(), bufferSize);
					builder.write(out, writeBody);
					out.flush();
				} else if (method == HTTPMethod.TRACE) {
					// no-op
				} else if (method == HTTPMethod.CONNECT) {
					// no-op
				} else {
					HTTPResponseBuilder builder = null;

					if (path.isEmpty()) {
						var handler = rootHandlers.get(method);

						if (handler != null) {
							req.init(this, "", new String[0], CompiledPath.EMPTY, headers, queryString, query, in);
							builder = createBuilder(req, handler.handler());
						}
					} else {
						var hl = handlers.get(method);

						if (hl != null) {
							var pathParts = path.split("/");

							for (int i = 0; i < pathParts.length; i++) {
								try {
									if (pathParts[i].indexOf('%') != -1) {
										pathParts[i] = URLDecoder.decode(pathParts[i], StandardCharsets.UTF_8);
									}
								} catch (Exception ex) {
									throw new InvalidPathException(ex.getMessage());
								}
							}

							var h = hl.staticHandlers().get(path);

							if (h != null) {
								req.init(this, path, pathParts, h.path(), headers, queryString, query, in);
								builder = createBuilder(req, h.handler());
							} else {
								for (var dynamicHandler : hl.dynamicHandlers()) {
									var matches = dynamicHandler.path().matches(pathParts);

									if (matches != null) {
										req.init(this, path, matches, dynamicHandler.path(), headers, queryString, query, in);
										builder = createBuilder(req, dynamicHandler.handler());
										break;
									}
								}
							}
						}
					}

					if (builder == null) {
						builder = createBuilder(req, null);
						builder.setStatus(HTTPStatus.NOT_FOUND);
					}

					out = new BufferedOutputStream(socket.getOutputStream(), bufferSize);
					builder.write(out, writeBody);
					out.flush();

					upgradedToWebSocket = (WSSession) builder.wsSession();

					if (upgradedToWebSocket != null) {
						upgradedToWebSocket.start(socket, in, out);
						upgradedToWebSocket.onOpen(req);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (upgradedToWebSocket == null) {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception ignored) {
			}

			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception ignored) {
			}

			try {
				if (socket != null) {
					socket.close();
				}
			} catch (Exception ignored) {
			}
		}
	}

	public HTTPResponseBuilder createBuilder(REQ req, @Nullable HTTPHandler<REQ> handler) {
		var builder = new HTTPResponseBuilder();

		if (serverName != null && !serverName.isEmpty()) {
			builder.setHeader("Server", serverName);
		}

		builder.setHeader("Date", HTTPResponseBuilder.DATE_TIME_FORMATTER.format(Instant.now()));

		if (handler != null) {
			try {
				var response = handler.handle(req);
				builder.setResponse(response);
				req.afterResponse(builder, response);
			} catch (Exception ex) {
				builder.setStatus(HTTPStatus.INTERNAL_ERROR);
				req.handlePayloadError(builder, ex);
			}
		}

		return builder;
	}
}
