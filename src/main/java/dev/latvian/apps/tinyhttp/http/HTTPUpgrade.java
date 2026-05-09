package dev.latvian.apps.tinyhttp.http;

public interface HTTPUpgrade<REQ extends HTTPRequest> {
	String protocol();

	void start(REQ req);

	boolean isClosed();
}
