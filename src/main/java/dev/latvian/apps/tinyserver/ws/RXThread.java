package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.StatusCode;

import java.nio.ByteBuffer;
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
				var info = FrameInfo.read(session.connection);
				Frame frame;

				if (info.size() == 0) {
					frame = new Frame(info, Frame.EMPTY_PAYLOAD);
				} else {
					var payload = new byte[info.size()];
					session.connection.readBytes(payload);
					frame = new Frame(info, payload);
					frame.applyMask();
				}

				switch (info.opcode()) {
					case CONTINUOUS, TEXT, BINARY -> {
						lastFrame = frame.appendTo(lastFrame);

						if (info.fin()) {
							switch (lastFrame.info().opcode()) {
								case TEXT -> session.onTextMessage(new String(lastFrame.payload(), StandardCharsets.UTF_8));
								case BINARY -> session.onBinaryMessage(lastFrame.payload());
							}

							lastFrame = null;
						}
					}
					case PING -> {
						session.onPing(frame.payload());
						session.send(new Frame(new FrameInfo(Opcode.PONG, info.mask(), info.fin(), info.rsv1(), info.rsv2(), info.rsv3(), info.maskKey(), info.size()), frame.payload()));
					}
					case PONG -> session.onPong(frame.payload());
					case CLOSING -> {
						if (info.size() > 0) {
							var payload = ByteBuffer.wrap(frame.payload());
							var code = payload.getShort();
							session.txThread.closeReason = new StatusCode(code, StandardCharsets.UTF_8.decode(payload).toString());
						} else {
							session.txThread.closeReason = WSCloseStatus.CLOSED.statusCode;
						}

						session.txThread.remoteClosed = true;
						session.send(Frame.simple(Opcode.CLOSING, null, frame.payload()));
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
