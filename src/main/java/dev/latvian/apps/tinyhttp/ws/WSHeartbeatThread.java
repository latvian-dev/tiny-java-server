package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.HTTPServer;
import dev.latvian.apps.tinyhttp.http.HTTPRequest;

import java.util.List;

public class WSHeartbeatThread<REQ extends HTTPRequest> extends Thread {
	public final HTTPServer<REQ> server;
	public final List<WSHandler<REQ, ?>> handlers;
	public final long heartbeatInterval;

	public WSHeartbeatThread(HTTPServer<REQ> server, List<WSHandler<REQ, ?>> handlers, long heartbeatInterval) {
		super(server.getServerName() + "-WS-Heartbeat");
		this.server = server;
		this.handlers = handlers;
		this.heartbeatInterval = heartbeatInterval;
		setDaemon(true);
	}

	@Override
	public void run() {
		while (server.isRunning()) {
			long now = System.currentTimeMillis();

			for (var handler : handlers) {
				try {
					for (var session : handler) {
						session.sendHeartbeat();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			long diff = System.currentTimeMillis() - now;
			long sleep = heartbeatInterval - diff;

			if (sleep > 0L) {
				try {
					Thread.sleep(sleep);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}