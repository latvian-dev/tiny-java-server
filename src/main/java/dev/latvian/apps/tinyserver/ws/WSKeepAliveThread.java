package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.HTTPServer;
import dev.latvian.apps.tinyserver.http.HTTPRequest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WSKeepAliveThread<REQ extends HTTPRequest> extends Thread {
	private static final AtomicInteger COUNTER = new AtomicInteger(0);

	public final HTTPServer<REQ> server;
	public final List<WSHandler<REQ, ?>> handlers;

	public WSKeepAliveThread(HTTPServer<REQ> server, List<WSHandler<REQ, ?>> handlers) {
		super("WSKeepAliveThread-" + COUNTER.incrementAndGet());
		this.server = server;
		this.handlers = handlers;
		setDaemon(true);
	}

	@Override
	public void run() {
		while (server.isRunning()) {
			for (var handler : handlers) {
				try {
					for (var session : handler) {
						session.sendPing(new byte[0]);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			try {
				Thread.sleep(20_000L);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}