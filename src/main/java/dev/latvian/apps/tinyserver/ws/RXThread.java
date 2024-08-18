package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.StatusCode;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.LockSupport;

class RXThread extends Thread {
	private final WSSession<?> session;
	private Frame lastFrame;

	public RXThread(WSSession<?> session) {
		super("WSSession-" + session.id + "-RX");
		this.session = session;
	}

	@Override
	public void run() {
		while (session.txThread.closeReason == null) {
			try {
				var frame = Frame.read(session.txThread.in);

				if (frame == null) {
					break;
				}

				var payload = frame.payload();

				switch (frame.opcode()) {
					case CONTINUOUS, TEXT, BINARY -> {
						lastFrame = frame.appendTo(lastFrame);

						if (frame.fin()) {
							switch (lastFrame.opcode()) {
								case TEXT -> session.onTextMessage(new String(lastFrame.payload(), StandardCharsets.UTF_8));
								case BINARY -> session.onBinaryMessage(lastFrame.payload());
							}

							lastFrame = null;
						}
					}
					case PING -> session.send(new Frame(Opcode.PONG, frame.mask(), frame.fin(), frame.rsv1(), frame.rsv2(), frame.rsv3(), payload));
					case CLOSING -> {
						if (payload.length > 0) {
							var code = (payload[0] << 8) | payload[1];
							session.txThread.closeReason = new StatusCode(code, new String(payload, 2, payload.length - 2, StandardCharsets.UTF_8));
						} else {
							session.txThread.closeReason = WSCloseStatus.CLOSED.statusCode;
						}

						session.txThread.remoteClosed = true;
						session.send(Frame.simple(Opcode.CLOSING, false, payload));
						session.rxThread = null;
						LockSupport.unpark(session.txThread);
					}
				}
			} catch (Exception ex) {
				session.onError(ex);
			}
		}
	}
}
