package dev.latvian.apps.tinyhttp.util;

import dev.latvian.apps.tinyhttp.HTTPServer;
import dev.latvian.apps.tinyhttp.OutputOperation;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.LinkedList;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class OutputOperations implements Runnable {
	private static final long PARK_TIME = Duration.ofMinutes(1L).toNanos();

	public final HTTPServer<?> server;
	public final String name;
	public final ReentrantLock lock;
	public final LinkedList<OutputOperation> queue;
	private ByteBuffer tempHeapBuffer;
	private ByteBuffer tempDirectBuffer;
	Thread thread;

	public OutputOperations(HTTPServer<?> server, String name) {
		this.server = server;
		this.name = name;
		this.lock = new ReentrantLock();
		this.queue = new LinkedList<>();
		this.tempHeapBuffer = ByteBufferUtils.allocate(64, false);
		this.tempDirectBuffer = ByteBufferUtils.allocate(1, true);
	}

	public void queue(OutputOperation operation) {
		lock.lock();

		try {
			if (thread == null) {
				thread = new Thread(this, server.getServerName() + "-" + name);
				queue.add(operation);
				thread.setDaemon(true);
				thread.start();
			} else {
				queue.add(operation);
				LockSupport.unpark(thread);
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean isRunning() {
		return server.isRunning();
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void run() {
		int emptyLoops = 0;

		while (isRunning()) {
			lock.lock();

			try {
				OutputOperation operation;

				while ((operation = queue.poll()) != null) {
					operation.write(this);
					emptyLoops = 0;
				}
			} finally {
				lock.unlock();
			}

			LockSupport.parkNanos(PARK_TIME);

			if (queue.isEmpty()) {
				emptyLoops++;

				if (emptyLoops >= 3) {
					break;
				}
			}
		}

		thread = null;
		// System.out.println("Closing thread " + name);
	}

	public ByteBuffer allocateHeap(int len) {
		return (tempHeapBuffer = ByteBufferUtils.grow(tempHeapBuffer, len, false));
	}

	public ByteBuffer allocateDirect(int len) {
		return (tempDirectBuffer = ByteBufferUtils.grow(tempDirectBuffer, len, true));
	}
}
