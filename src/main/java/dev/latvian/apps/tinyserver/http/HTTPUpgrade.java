package dev.latvian.apps.tinyserver.http;

public interface HTTPUpgrade<REQ extends HTTPRequest> {
	String protocol();

	void start(REQ req);

	boolean isClosed();
}
