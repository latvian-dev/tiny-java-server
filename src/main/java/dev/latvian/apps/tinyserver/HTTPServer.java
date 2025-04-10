package dev.latvian.apps.tinyserver;

import dev.latvian.apps.tinyserver.error.BindFailedException;
import dev.latvian.apps.tinyserver.error.InvalidPathException;
import dev.latvian.apps.tinyserver.http.HTTPHandler;
import dev.latvian.apps.tinyserver.http.HTTPMethod;
import dev.latvian.apps.tinyserver.http.HTTPPathHandler;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.HTTPUpgrade;
import dev.latvian.apps.tinyserver.http.Header;
import dev.latvian.apps.tinyserver.http.response.HTTPPayload;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;
import dev.latvian.apps.tinyserver.http.response.error.HTTPError;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class HTTPServer<REQ extends HTTPRequest> implements Runnable, ServerRegistry<REQ> {
	private static final Object DUMMY = new Object();
	private static final int[] DEFAULT_PORTS = {8080};

	private final Supplier<REQ> requestFactory;
	private final Map<HTTPMethod, HandlerList<REQ>> handlers;
	private final Map<HTTPMethod, HTTPPathHandler<REQ>> rootHandlers;
	private final IdentityHashMap<HTTPConnection<REQ>, Object> connections;
	private final Set<HTTPConnection<REQ>> publicConnections;
	private String serverName;
	//private Selector selector;
	private ServerSocketChannel serverSocketChannel;
	private String address;
	private int[] ports = DEFAULT_PORTS;
	private boolean daemon = false;
	private int bufferSize = 0;
	private int maxKeepAliveConnections = 100;
	long now;
	int keepAliveTimeout = 15;

	public HTTPServer(Supplier<REQ> requestFactory) {
		this.requestFactory = requestFactory;
		this.handlers = new EnumMap<>(HTTPMethod.class);
		this.rootHandlers = new EnumMap<>(HTTPMethod.class);
		this.connections = new IdentityHashMap<>();
		this.publicConnections = Collections.unmodifiableSet(connections.keySet());
		this.serverName = "dev.latvian.apps:tiny-java-server";
	}

	public void setServerName(String name) {
		this.serverName = name;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setPort(int port) {
		this.ports = new int[]{port};
	}

	public void setPort(IntStream range) {
		this.ports = range.toArray();
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void setMaxKeepAliveConnections(int max) {
		this.maxKeepAliveConnections = max;
	}

	public void setKeepAliveTimeout(Duration duration) {
		this.keepAliveTimeout = (int) duration.toSeconds();
	}

	public boolean isRunning() {
		return serverSocketChannel != null;
	}

	public int start() {
		if (serverSocketChannel != null) {
			throw new IllegalStateException("Server is already running");
		} else if (ports.length == 0) {
			return -1;
		}

		int boundPort = -1;

		Arrays.sort(ports);
		int minPort = ports[0];
		int maxPort = ports[ports.length - 1];

		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);

			var socket = serverSocketChannel.socket();

			if (bufferSize > 0) {
				socket.setReceiveBufferSize(bufferSize);
			}

			var inetAddress = address == null ? null : InetAddress.getByName(address);

			for (int i : ports) {
				try {
					socket.bind(new InetSocketAddress(inetAddress, i));
					boundPort = i;
					break;
				} catch (Exception ignore) {
				}
			}

			// selector = Selector.open();
			// serverSocketChannel.register(selector, serverSocketChannel.validOps());
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		if (boundPort == -1) {
			throw new BindFailedException(minPort, maxPort);
		}

		startThread();
		return boundPort;
	}

	public void stop() {
		serverSocketChannel = null;
	}

	@Override
	public void http(HTTPMethod method, String path, HTTPHandler<REQ> handler) {
		var compiledPath = CompiledPath.compile(path);
		var pathHandler = new HTTPPathHandler<>(method, compiledPath, handler);

		if (compiledPath == CompiledPath.EMPTY) {
			if (!rootHandlers.containsKey(method)) {
				rootHandlers.put(method, pathHandler);
			}
		} else {
			var hl = handlers.computeIfAbsent(method, HandlerList::new);

			if (compiledPath.variables() > 0) {
				hl.dynamicHandlers().add(pathHandler);
			} else {
				var p = Arrays.stream(compiledPath.parts()).map(CompiledPath.Part::name).collect(Collectors.joining("/"));

				if (!hl.staticHandlers().containsKey(p)) {
					hl.staticHandlers().put(p, pathHandler);
				}
			}
		}
	}

	@Override
	public final void run() {
		serverStarted();

		try (var ssc = serverSocketChannel) {
			while (isRunning()) {
				now = System.currentTimeMillis();
				acceptClient(ssc);
				connections.keySet().removeIf(HTTPConnection::handleClosure);
			}

			serverStopped(null);
		} catch (Exception ex) {
			serverStopped(ex);
		}
	}

	private void acceptClient(ServerSocketChannel ssc) {
		SocketChannel socketChannel = null;

		try {
			socketChannel = ssc.accept();

			if (socketChannel != null) {
				socketChannel.socket().setSoTimeout((keepAliveTimeout + 1) * 1000);
				var connection = createConnection(socketChannel, Instant.now());
				connection.lastActivity = now;
				connections.put(connection, DUMMY);
				queueSession(connection);
			}
		} catch (Exception ex) {
			if (socketChannel != null) {
				try {
					socketChannel.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

	protected void serverStarted() {
	}

	protected void serverStopped(@Nullable Throwable ex) {
		if (ex != null) {
			ex.printStackTrace();
		}
	}

	boolean handleClient(HTTPConnection<REQ> connection) {
		connection.lastActivity = System.currentTimeMillis();
		connection.beforeHandshake();

		boolean keepAlive = false;

		try {
			var firstLineStr = connection.readCRLF();

			if (!firstLineStr.toLowerCase().endsWith(" http/1.1")) {
				connection.status = HTTPConnection.INVALID_REQUEST;
				return false;
			}

			var startTime = Instant.now();

			firstLineStr = firstLineStr.substring(0, firstLineStr.length() - 9).trim();
			var firstLine = firstLineStr.split(" ", 2);

			var method = firstLine.length == 2 ? HTTPMethod.fromString(firstLine[0]) : null;
			boolean writeBody = method != null && method.body();
			var originalMethod = method;

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

				Map<String, OptionalString> query = queryString.isEmpty() ? Map.of() : new HashMap<>();

				if (!queryString.isEmpty()) {
					for (var part : queryString.split("&")) {
						var parts = part.split("=", 2);

						if (parts.length == 2) {
							query.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), OptionalString.of(URLDecoder.decode(parts[1], StandardCharsets.UTF_8)));
						} else {
							query.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), OptionalString.EMPTY);
						}
					}
				}

				var headers = new ArrayList<Header>();

				while (true) {
					var line = connection.readCRLF();

					if (line.isEmpty()) {
						break;
					}

					var parts = line.split(":", 2);

					if (parts.length == 2) {
						var header = new Header(parts[0].trim(), parts[1].trim());
						headers.add(header);

						if (header.is("Connection") && header.value().asString().equalsIgnoreCase("keep-alive")) {
							keepAlive = true;
						}
					}
				}

				var req = requestFactory.get();
				req.preInit(connection, startTime, originalMethod);

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
					builder.addHeader("Allow", allowed.stream().map(HTTPMethod::name).collect(Collectors.joining(",")));
					builder.process(req, keepAliveTimeout, keepAlive ? maxKeepAliveConnections : 0);
					builder.write(connection, writeBody);
				} else if (method == HTTPMethod.TRACE) {
					// no-op
				} else if (method == HTTPMethod.CONNECT) {
					// no-op
				} else {
					HTTPPayload builder = null;

					if (path.isEmpty()) {
						var handler = rootHandlers.get(method);

						if (handler != null) {
							req.init("", new String[0], CompiledPath.EMPTY, headers, queryString, query);
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
								req.init(path, pathParts, h.path(), headers, queryString, query);
								builder = createBuilder(req, h.handler());
							} else {
								for (var dynamicHandler : hl.dynamicHandlers()) {
									var matches = dynamicHandler.path().matches(pathParts);

									if (matches != null) {
										req.init(path, matches, dynamicHandler.path(), headers, queryString, query);
										builder = createBuilder(req, dynamicHandler.handler());
										break;
									}
								}
							}
						}
					}

					if (builder == null) {
						builder = createBuilder(req, null);
					}

					builder.process(req, keepAliveTimeout, keepAlive ? maxKeepAliveConnections : 0);
					builder.write(connection, writeBody);

					connection.upgrade = (HTTPUpgrade) builder.getUpgrade();

					if (connection.upgrade != null) {
						connection.upgrade.start(req);
					}
				}
			}
		} catch (Throwable ex) {
			connection.error(ex);
		}

		return keepAlive && connection.upgrade == null;
	}

	public HTTPPayload createBuilder(REQ req, @Nullable HTTPHandler<REQ> handler) {
		var payload = new HTTPPayload(serverName, Instant.now());
		var preResponse = req.createPreResponse(handler);

		if (preResponse != null) {
			payload.setResponse(preResponse);
			return payload;
		}

		if (handler != null) {
			HTTPResponse response;
			Throwable error;

			try {
				response = handler.handle(req);
				error = null;
			} catch (Throwable error1) {
				response = (error1 instanceof HTTPError h ? h.getStatus() : HTTPStatus.INTERNAL_ERROR).defaultResponse();
				error = error1;
			}

			var res = req.handleResponse(payload, response, error);
			payload.setResponse(res);
			req.afterResponse(payload, res, handler, error);
		} else {
			var res = HTTPStatus.NOT_FOUND.defaultResponse();
			payload.setResponse(res);
			req.afterResponse(payload, res, null, null);
		}

		return payload;
	}

	protected HTTPConnection<REQ> createConnection(SocketChannel socketChannel, Instant createdTime) throws IOException {
		return new HTTPConnection<>(this, socketChannel, createdTime);
	}

	protected void startThread() {
		var thread = new Thread(this, serverName == null || serverName.isEmpty() ? ("HTTPServer-" + System.currentTimeMillis()) : serverName);
		thread.setDaemon(daemon);
		thread.start();
	}

	protected void queueSession(HTTPConnection<REQ> session) {
		Thread.startVirtualThread(session);
	}

	public Set<HTTPConnection<REQ>> connections() {
		return publicConnections;
	}

	public Stream<HTTPPathHandler<REQ>> handlers() {
		return handlers.values().stream().flatMap(h -> Stream.concat(h.staticHandlers().values().stream(), h.dynamicHandlers().stream()));
	}
}
