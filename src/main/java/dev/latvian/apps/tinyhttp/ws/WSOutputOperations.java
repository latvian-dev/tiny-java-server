package dev.latvian.apps.tinyhttp.ws;

import dev.latvian.apps.tinyhttp.util.OutputOperations;

public class WSOutputOperations extends OutputOperations {
	private final WSSession<?> session;

	public WSOutputOperations(WSSession<?> session, String name) {
		super(session.connection.server(), name);
		this.session = session;
	}

	@Override
	public boolean isRunning() {
		return !session.closed && super.isRunning();
	}
}
