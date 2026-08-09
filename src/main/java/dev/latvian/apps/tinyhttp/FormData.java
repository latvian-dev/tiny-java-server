package dev.latvian.apps.tinyhttp;

import dev.latvian.apps.tinyhttp.http.body.Body;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record FormData(List<NamedString> values, List<Upload> uploads) {
	public static final FormData EMPTY = new FormData(List.of(), List.of());

	public boolean hasValue(String name) {
		for (var ns : values) {
			if (ns.is(name)) {
				return true;
			}
		}

		return false;
	}

	public OptionalString value(String name) {
		for (var ns : values) {
			if (ns.is(name)) {
				return ns.value();
			}
		}

		return OptionalString.MISSING;
	}

	public List<Upload> uploads(String name) {
		var list = new ArrayList<Upload>(1);

		for (var upload : uploads) {
			if (upload.is(name)) {
				list.add(upload);
			}
		}

		return list;
	}

	public boolean hasUpload(String name) {
		for (var ns : uploads) {
			if (ns.is(name)) {
				return true;
			}
		}

		return false;
	}

	@Nullable
	public Upload upload(String name) {
		for (var upload : uploads) {
			if (upload.is(name)) {
				return upload;
			}
		}

		return null;
	}

	@Nullable
	public <T> T uploadAs(String name, Function<Body, T> convert) {
		var upload = upload(name);

		if (upload != null) {
			return convert.apply(upload.body());
		}

		return null;
	}
}
