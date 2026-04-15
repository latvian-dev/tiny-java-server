package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.HTTPServer;
import dev.latvian.apps.tinyserver.http.HTTPRequest;

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
			for (var handler : handlers) {
				try {
					for (var session : handler) {
						session.sendHeartbeat();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			try {
				Thread.sleep(heartbeatInterval);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}