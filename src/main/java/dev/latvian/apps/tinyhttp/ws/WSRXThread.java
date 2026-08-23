package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.CloseReason;
import dev.latvian.apps.tinyhttp.HTTPServer;
import dev.latvian.apps.tinyhttp.StatusCode;
import dev.latvian.apps.tinyhttp.util.ByteBufferUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

class WSRXThread extends Thread {
	private final WSSession<?> session;
	private Frame lastFrame;
	private ByteBuffer inputBuffer;

	public WSRXThread(HTTPServer<?> server, WSSession<?> session) {
		super(server.getServerName() + "-WS-RX-" + session.key);
		this.session = session;
	}

	@Override
	public void run() {
		while (session.rxThread == this) {
			try {
				var info = FrameInfo.read(session.connection);
				int size = info.size();
				Frame frame;

				if (size == 0) {
					frame = new Frame(info, ByteBufferUtils.EMPTY_HEAP);
				} else {
					inputBuffer = ByteBufferUtils.grow(inputBuffer, size, true);
					session.connection.read(inputBuffer);
					inputBuffer.rewind();
					info.applyMask(inputBuffer, 0, size);
					frame = new Frame(info, inputBuffer);
				}

				switch (info.opcode()) {
					case CONTINUOUS, TEXT, BINARY -> {
						var lframe = frame.appendTo(lastFrame);

						if (info.fin()) {
							lastFrame = null;

							var response = switch (lframe.info().opcode()) {
								case TEXT -> session.onTextMessage(lframe.payload());
								case BINARY -> session.onBinaryMessage(lframe.payload());
								default -> null;
							};

							if (response != null) {
								session.sendNow(response);
							}
						} else {
							lastFrame = lframe;
						}
					}
					case PING -> {
						var payload = frame.copyPayload();
						session.onPing(frame.payload());
						session.sendNow(new Frame(new FrameInfo(Opcode.PONG, info.mask(), info.fin(), info.rsv1(), info.rsv2(), info.rsv3(), info.maskKey(), info.size()), payload));
					}
					case PONG -> session.onPong(frame.payload());
					case CLOSING -> {
						session.sendNow(Frame.simple(Opcode.CLOSING, null, frame.copyPayload()));

						if (info.size() > 0) {
							var payload = frame.payload();
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
