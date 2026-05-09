package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.OutputOperation;
import dev.latvian.apps.tinyhttp.http.HTTPRequest;
import dev.latvian.apps.tinyhttp.util.OutputOperations;

public record QueuedFrame<REQ extends HTTPRequest>(WSSession<REQ> session, Frame frame) implements OutputOperation {
	@Override
	public boolean write(OutputOperations operations) {
		return frame().write(session, operations);
	}
}
