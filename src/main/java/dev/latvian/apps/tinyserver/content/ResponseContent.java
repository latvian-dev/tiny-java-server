package dev.latvian.apps.tinyserver.content;

import java.io.OutputStream;

public interface ResponseContent {
	default long length() {
		return -1L;
	}

	default String type() {
		return "";
	}

	void write(OutputStream out) throws Exception;
}
