package dev.latvian.apps.tinyhttp;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record FormData(List<NamedString> values, List<Upload> uploads) {
	public static final FormData EMPTY = new FormData(List.of(), List.of());

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
			if (upload.name().equalsIgnoreCase(name)) {
				list.add(upload);
			}
		}

		return list;
	}

	@Nullable
	public Upload upload(String name) {
		for (var upload : uploads) {
			if (upload.name().equalsIgnoreCase(name)) {
				return upload;
			}
		}

		return null;
	}
}
