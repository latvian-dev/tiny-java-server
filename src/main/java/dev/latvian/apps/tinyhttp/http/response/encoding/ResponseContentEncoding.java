package dev.latvian.apps.tinyhttp.http.response.encoding;

import dev.latvian.apps.tinyhttp.content.ResponseContent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface ResponseContentEncoding {
	static List<ResponseContentEncoding> defaultEncodingMethods() {
		var list = new ArrayList<ResponseContentEncoding>(3);

		if (ZSTDResponseContentEncoding.AVAILABLE) {
			list.add(ZSTDResponseContentEncoding.INSTANCE);
		}

		list.add(GZIPResponseContentEncoding.INSTANCE);
		list.add(DeflateResponseContentEncoding.INSTANCE);
		return list;
	}

	String name();

	ResponseContent encode(ResponseContent body) throws IOException;
}
