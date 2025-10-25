package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.HTTPServer;

public class WSKeepAliveThread extends Thread {
	public final HTTPServer<?> server;
	public final WSHandler<?, ?> handler;

	public WSKeepAliveThread(HTTPServer<?> server, WSHandler<?, ?> handler, String name) {
		super("WSKeepAliveThread-" + name);
		this.server = server;
		this.handler = handler;
		setDaemon(true);
	}

	@Override
	public void run() {
		while (server.isRunning()) {
			try {
				for (var session : handler) {
					session.sendPing(new byte[0]);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try {
				Thread.sleep(10000L);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}