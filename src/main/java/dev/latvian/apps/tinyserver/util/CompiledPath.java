package dev.latvian.apps.tinyserver.util;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public record CompiledPath(Part[] parts, String string, int variables, boolean wildcard) {
	public static final CompiledPath EMPTY = new CompiledPath(new Part[0], "", 0, false);
	public static final CompiledPath STAR = new CompiledPath(new Part[]{new Part("*", false, false)}, "*", 0, false);

	public record Part(String name, boolean variable, boolean wildcard) {
		public boolean matches(String string) {
			return variable || name.equals(string);
		}
	}

	public static CompiledPath compile(String string) {
		if (string.isEmpty()) {
			return EMPTY;
		} else if (string.length() == 1) {
			var c = string.charAt(0);

			if (c == '/' || c == '\\') {
				return EMPTY;
			} else if (c == '*') {
				return STAR;
			}
		}

		var ostring = string;

		string = string.trim().replace('\\', '/');

		while (string.startsWith("/")) {
			string = string.substring(1);
		}

		while (string.endsWith("/")) {
			string = string.substring(0, string.length() - 1);
		}

		if (string.isEmpty()) {
			return EMPTY;
		}

		var partsStr = string.split("/");
		var parts = new ArrayList<Part>(partsStr.length);
		var toString = new ArrayList<String>();
		boolean wildcard = false;
		int variables = 0;

		for (var s : partsStr) {
			if (s.isEmpty()) {
				continue;
			} else if (wildcard) {
				throw new IllegalArgumentException("<wildcard> argument must be the last part of the path '" + ostring + "'");
			}

			if (s.startsWith("{") && s.endsWith("}")) {
				parts.add(new Part(s.substring(1, s.length() - 1), true, false));
				variables++;
			} else if (s.startsWith("<") && s.endsWith(">")) {
				parts.add(new Part(s.substring(1, s.length() - 1), true, true));
				variables++;
				wildcard = true;
			} else {
				parts.add(new Part(s, false, false));
			}

			toString.add(s);
		}

		return parts.isEmpty() ? EMPTY : new CompiledPath(parts.toArray(new Part[0]), String.join("/", toString), variables, wildcard);
	}

	@Nullable
	public String[] matches(String[] path) {
		if (wildcard) {
			if (path.length >= parts.length) {
				for (int i = 0; i < parts.length; i++) {
					if (!parts[i].matches(path[i])) {
						return null;
					}
				}

				if (path.length == parts.length) {
					return path;
				} else {
					var joinedPath = new String[parts.length];
					System.arraycopy(path, 0, joinedPath, 0, parts.length);

					for (int i = parts.length; i < path.length; i++) {
						joinedPath[parts.length - 1] += "/" + path[i];
					}

					return joinedPath;
				}
			}
		} else {
			if (path.length == parts.length) {
				for (int i = 0; i < parts.length; i++) {
					if (!parts[i].matches(path[i])) {
						return null;
					}
				}

				return path;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return string;
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof CompiledPath p && string.equals(p.string);
	}
}
