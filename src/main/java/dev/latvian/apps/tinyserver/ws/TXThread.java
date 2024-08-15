package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.StatusCode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Deque;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.LockSupport;

class TXThread extends Thread {
	private final WSSession<?> session;
	private final Socket socket;
	final InputStream in;
	private final OutputStream out;
	StatusCode closeReason;
	boolean remoteClosed;
	Deque<Frame> queue;
	private final Random random;

	public TXThread(WSSession<?> session, Socket socket, InputStream in, OutputStream out) {
		super("WSSession-" + session.id + "-TX");
		this.session = session;
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.queue = new ConcurrentLinkedDeque<>();
		this.random = new Random();
	}

	@Override
	public void run() {
		while (session.txThread == this) {
			var p = queue.poll();

			if (p != null) {
				try {
					p.write(random, out);
				} catch (IOException e) {
					session.onError(e);
					break;
				}
			} else {
				try {
					out.flush();
				} catch (IOException ignored) {
					break;
				}

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

		try {
			in.close();
		} catch (Exception ignore) {
		}

		try {
			out.close();
		} catch (Exception ignore) {
		}

		try {
			socket.close();
		} catch (Exception ignore) {
		}
	}
}
