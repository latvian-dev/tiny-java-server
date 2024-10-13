package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.StatusCode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.LockSupport;

class TXThread extends Thread {
	private final WSSession<?> session;
	StatusCode closeReason;
	boolean remoteClosed;
	Deque<Frame> queue;

	public TXThread(WSSession<?> session) {
		super("WSSession-" + session.id + "-TX");
		this.session = session;
		this.queue = new ConcurrentLinkedDeque<>();
	}

	@Override
	public void run() {
		var infoBuf = ByteBuffer.allocate(0);

		while (session.txThread == this) {
			var frame = queue.poll();

			if (frame != null) {
				try {
					int len = frame.info().bytes();

					if (len > infoBuf.capacity()) {
						infoBuf = ByteBuffer.allocate(len);
					} else {
						infoBuf.clear();
					}

					frame.info().put(infoBuf);
					infoBuf.flip();
					session.connection.socketChannel.write(infoBuf);

					if (frame.info().size() > 0L) {
						frame.applyMask();
						session.connection.socketChannel.write(frame.payload());
					}
				} catch (IOException e) {
					session.onError(e);
					break;
				}
			} else {
				if (closeReason != null) {
					break;
				} else {
					LockSupport.park();
				}
			}
		}

		session.sessionMap.remove(session.id);
		session.rxThread = null;
		session.onClose(closeReason, remoteClosed);
		session.connection.close();
	}
}
