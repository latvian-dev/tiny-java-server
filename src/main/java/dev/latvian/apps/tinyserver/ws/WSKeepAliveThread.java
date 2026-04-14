package dev.latvian.apps.tinyserver.ws;

import java.util.concurrent.atomic.AtomicInteger;

public class WSKeepAliveThread extends Thread {
	private static final AtomicInteger COUNTER = new AtomicInteger(0);

	public final WSHandler<?, ?> handler;

	public WSKeepAliveThread(WSHandler<?, ?> handler) {
		super("WSKeepAliveThread-" + COUNTER.incrementAndGet());
		this.handler = handler;
		setDaemon(true);
	}

	@Override
	public void run() {
		while (handler.server().isRunning()) {
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