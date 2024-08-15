package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.http.HTTPRequest;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class EmptyWSHandler implements WSHandler<HTTPRequest, WSSession<HTTPRequest>> {
	public static final EmptyWSHandler INSTANCE = new EmptyWSHandler();

	@Override
	public Map<UUID, WSSession<HTTPRequest>> sessions() {
		return Map.of();
	}

	@Override
	public void broadcast(Frame frame) {
	}

	@Override
	public void broadcastText(String payload) {
	}

	@Override
	public void broadcastText(Supplier<String> payload) {
	}

	@Override
	public void broadcastBinary(byte[] payload) {
	}

	@Override
	public void broadcastBinary(Supplier<byte[]> payload) {
	}
}
