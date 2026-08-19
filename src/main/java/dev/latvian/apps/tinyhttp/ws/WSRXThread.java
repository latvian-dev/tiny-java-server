package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.CloseReason;
import dev.latvian.apps.tinyhttp.HTTPServer;
import dev.latvian.apps.tinyhttp.StatusCode;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

class WSRXThread extends Thread {
	private final WSSession<?> session;
	private Frame lastFrame;

	public WSRXThread(HTTPServer<?> server, WSSession<?> session) {
		super(server.getServerName() + "-WS-RX-" + session.key);
		this.session = session;
	}

	@Override
	public void run() {
		while (session.rxThread == this) {
			try {
				var info = FrameInfo.read(session.connection);
				Frame frame;

				if (info.size() == 0) {
					frame = new Frame(info, Frame.EMPTY_PAYLOAD);
				} else {
					var payload = new byte[info.size()];
					session.connection.readBytes(payload);
					info.applyMask(payload, 0, payload.length);
					frame = new Frame(info, payload);
				}

				switch (info.opcode()) {
					case CONTINUOUS, TEXT, BINARY -> {
						lastFrame = frame.appendTo(lastFrame);

						if (info.fin()) {
							var response = switch (lastFrame.info().opcode()) {
								case TEXT -> session.onTextMessage(lastFrame.payload());
								case BINARY -> session.onBinaryMessage(lastFrame.payload());
								default -> null;
							};

							lastFrame = null;

							if (response != null) {
								session.sendNow(response);
							}
						}
					}
					case PING -> {
						session.onPing(frame.payload());
						session.sendNow(new Frame(new FrameInfo(Opcode.PONG, info.mask(), info.fin(), info.rsv1(), info.rsv2(), info.rsv3(), info.maskKey(), info.size()), frame.payload()));
					}
					case PONG -> session.onPong(frame.payload());
					case CLOSING -> {
						session.sendNow(Frame.simple(Opcode.CLOSING, null, frame.payload()));

						if (info.size() > 0) {
							var payload = ByteBuffer.wrap(frame.payload());
							var code = payload.getShort();
							session.close0(new CloseReason(new StatusCode(code, StandardCharsets.UTF_8.decode(payload).toString()), true), null);
						} else {
							session.close0(new CloseReason(WSCloseStatus.CLOSED.statusCode, true), null);
						}
					}
				}
			} catch (Throwable ex) {
				session.handleException(ex);
			}
		}
	}
}
