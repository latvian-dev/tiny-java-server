package dev.latvian.apps.tinyhttp;

import dev.latvian.apps.tinyhttp.http.body.Body;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Upload(String name, String fileName, List<NamedString> headers, Body body) {
	@Override
	public @NotNull String toString() {
		return "Upload[" + name + "=" + fileName + ", " + body.contentType() + ", " + body.contentLength() + " bytes]";
	}

	public boolean is(String name) {
		return this.name.equalsIgnoreCase(name);
	}

	public boolean hasHeader(String name) {
		for (var ns : headers) {
			if (ns.is(name)) {
				return true;
			}
		}

		return false;
	}

	public OptionalString header(String name) {
		for (var ns : headers) {
			if (ns.is(name)) {
				return ns.value();
			}
		}

		return OptionalString.MISSING;
	}
}
