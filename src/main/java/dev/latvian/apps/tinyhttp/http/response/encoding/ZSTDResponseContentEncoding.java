package dev.latvian.apps.tinyhttp.http.response.encoding;

import com.github.luben.zstd.Zstd;
import dev.latvian.apps.tinyhttp.content.ByteContent;
import dev.latvian.apps.tinyhttp.content.ResponseContent;

import java.io.IOException;

public class ZSTDResponseContentEncoding implements ResponseContentEncoding {
	public static final boolean AVAILABLE = isAvailable();
	public static final ZSTDResponseContentEncoding INSTANCE = new ZSTDResponseContentEncoding();

	private static boolean isAvailable() {
		try {
			Class.forName("com.github.luben.zstd.Zstd");
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}

	private ZSTDResponseContentEncoding() {
	}

	@Override
	public String name() {
		return "zstd";
	}

	@Override
	public ResponseContent encode(ResponseContent body) throws IOException {
		return new ByteContent(compress(body.toBytes()), body.type());
	}

	private byte[] compress(byte[] input) {
		return Zstd.compress(input);
	}
}
