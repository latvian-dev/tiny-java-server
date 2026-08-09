package dev.latvian.apps.tinyhttp.util;

import dev.latvian.apps.tinyhttp.NamedString;

import java.util.ArrayList;
import java.util.List;

public class StringParser {
	@FunctionalInterface
	public interface CharPredicate {
		CharPredicate SPACE = c -> c <= ' ';
		CharPredicate SINGLE_QUOTE = c -> c == '\'';
		CharPredicate DOUBLE_QUOTE = c -> c == '"';
		CharPredicate EQ = c -> c == '=';
		CharPredicate BREAK = c -> c == ';';
		CharPredicate COL = c -> c == ':';

		boolean test(char c);
	}

	private final char[] chars;
	private final int limit;
	private int pos;

	private StringParser(char[] chars, int pos, int limit) {
		this.chars = chars;
		this.pos = pos;
		this.limit = limit;
	}

	public StringParser(String value) {
		this(value.toCharArray(), 0, value.length());
	}

	public int pos() {
		return pos;
	}

	public void pos(int pos) {
		this.pos = pos;
	}

	public char peek() {
		if (pos >= chars.length) {
			return 0;
		}

		return chars[pos];
	}

	public char peek(int ahead) {
		int p = pos + ahead;

		if (p < 0 || p >= limit) {
			return 0;
		}

		return chars[p];
	}

	public void next() {
		pos++;
	}

	public void skipWhitespace() {
		while (pos < limit && peek() <= ' ') {
			next();
		}
	}

	public boolean next(CharPredicate match) {
		if (match.test(peek())) {
			next();
			return true;
		}

		return false;
	}

	public String until(CharPredicate end) {
		var builder = new StringBuilder();

		while (pos < limit) {
			var c = peek();

			if (end.test(c)) {
				break;
			} else {
				builder.append(c);
				next();
			}
		}

		return builder.toString();
	}

	public String remaining() {
		return new String(chars, pos, limit - pos);
	}

	public StringParser fork() {
		return new StringParser(chars, pos, limit);
	}

	public String quotedString() {
		var p = peek();

		if (p == '"') {
			next();
			var str = until(CharPredicate.DOUBLE_QUOTE);
			next();
			return str;
		} else if (p == '\'') {
			next();
			var str = until(CharPredicate.SINGLE_QUOTE);
			next();
			return str;
		} else {
			return "";
		}
	}

	public boolean crlf() {
		if (peek() == '\r' && peek(1) == '\n') {
			next();
			next();
			return true;
		}

		return false;
	}

	public NamedString parameter() {
		var name = until(c -> c <= ' ' || c == '=' || c == ';');
		skipWhitespace();

		if (next(CharPredicate.EQ)) {
			skipWhitespace();
			var value = quotedString();
			return NamedString.of(name, value);
		} else {
			return NamedString.empty(name);
		}
	}

	public List<NamedString> parameters() {
		var list = new ArrayList<NamedString>(3);

		while (pos < limit) {
			skipWhitespace();
			var parameter = parameter();
			list.add(parameter);
			skipWhitespace();

			if (!next(CharPredicate.BREAK)) {
				break;
			}
		}

		return list;
	}
}
