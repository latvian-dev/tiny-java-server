package dev.latvian.apps.tinyhttp.content;

import dev.latvian.apps.tinyhttp.HTTPConnection;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public record FileContent(Path file, long length, String overrideType, @Nullable RequestRange range) implements ResponseContent {
	public FileContent(Path file, long length, String overrideType) {
		this(file, length, overrideType, null);
	}

	public FileContent(Path file, String overrideType) throws IOException {
		this(file, Files.size(file), overrideType);
	}

	@Override
	public long rangeLength() {
		return range == null ? length : range.length();
	}

	@Override
	public String type() {
		if (overrideType == null || overrideType.isEmpty()) {
			try {
				return Files.probeContentType(file);
			} catch (IOException ignore) {
				return "";
			}
		}

		return overrideType;
	}

	@Override
	public void write(OutputStream out) throws IOException {
		if (range == null) {
			Files.copy(file, out);
		} else {
			var start = range.start();
			var length = range.length();

			try (var channel = length < this.length ? Files.newByteChannel(file, StandardOpenOption.READ).truncate(start + length) : Files.newByteChannel(file, StandardOpenOption.READ);
			     var stream = Channels.newInputStream(channel)
			) {
				stream.skipNBytes(start);
				stream.transferTo(out);
			}
		}
	}

	@Override
	public byte[] toBytes() throws IOException {
		if (range == null) {
			return Files.readAllBytes(file);
		} else {
			return ResponseContent.super.toBytes();
		}
	}

	@Override
	public void transferTo(HTTPConnection<?> connection) throws IOException {
		if (range == null) {
			connection.write(file);
		} else {
			var length = range.length();
			connection.write0(file, range.start(), length, length < this.length);
		}
	}

	@Override
	public HttpRequest.BodyPublisher bodyPublisher() throws IOException {
		if (range == null) {
			return HttpRequest.BodyPublishers.ofFile(file);
		} else {
			return HttpRequest.BodyPublishers.ofInputStream(() -> {
				try {
					var channel = Files.newByteChannel(file, StandardOpenOption.READ).truncate(range.end() + 1L);
					channel.position(range.start());
					return Channels.newInputStream(channel);
				} catch (Exception ex) {
					throw new RuntimeException("Failed to create new file channel", ex);
				}
			});
		}
	}

	@Override
	public ResponseContent withRange(RequestRange range) {
		return new FileContent(file, length, overrideType, range);
	}
}
