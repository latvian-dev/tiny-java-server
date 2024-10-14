package dev.latvian.apps.tinyserver.http.response;

public record CookieResponse(HTTPResponse original, String key, String value, Builder builder) implements HTTPResponse {
	public static class Builder {
		private static final Builder DEFAULT = new Builder();

		private String domain = null;
		private int maxAge = -2;
		private boolean httpOnly = false;
		private boolean partitioned = false;
		private String path = null;
		private String sameSite = null;
		private boolean secure = false;
		private String comment = null;

		public Builder domain(String domain) {
			this.domain = domain;
			return this;
		}

		public Builder maxAge(int maxAge) {
			this.maxAge = maxAge;
			return this;
		}

		public Builder maxAgeYear() {
			return maxAge(31536000);
		}

		public Builder httpOnly() {
			this.httpOnly = true;
			return this;
		}

		public Builder partitioned(boolean partitioned) {
			this.partitioned = partitioned;
			return this;
		}

		public Builder path(String path) {
			this.path = path;
			return this;
		}

		public Builder sameSite(String sameSite) {
			this.sameSite = sameSite;
			return this;
		}

		public Builder secure() {
			this.secure = true;
			return this;
		}

		public Builder comment(String comment) {
			this.comment = comment;
			return this;
		}

		public Builder remove() {
			return maxAge(0);
		}

		public Builder session() {
			return maxAge(-1);
		}
	}

	public CookieResponse(HTTPResponse original, String key, String value) {
		this(original, key, value, Builder.DEFAULT);
	}

	@Override
	public HTTPStatus status() {
		return original.status();
	}

	@Override
	public void build(HTTPPayload payload) {
		var sb = new StringBuilder();
		sb.append(value);

		if (builder.domain != null) {
			sb.append("; Domain=").append(builder.domain);
		}

		if (builder.maxAge >= -1) {
			sb.append("; Max-Age=").append(builder.maxAge);
		}

		if (builder.httpOnly) {
			sb.append("; HttpOnly");
		}

		if (builder.partitioned) {
			sb.append("; Partitioned");
		}

		if (builder.path != null) {
			sb.append("; Path=").append(builder.path);
		}

		if (builder.sameSite != null) {
			sb.append("; SameSite=").append(builder.sameSite);
		}

		if (builder.secure) {
			sb.append("; Secure");
		}

		if (builder.comment != null) {
			sb.append("; Comment=").append(builder.comment);
		}

		payload.setCookie(key, sb.toString());
		original.build(payload);
	}
}
