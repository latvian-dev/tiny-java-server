package dev.latvian.apps.tinyserver.http.response;

public record HTTPResponseWithCookie(HTTPResponse original, String key, String value, Builder builder) implements HTTPResponse {
	public static class Builder {
		private static final Builder DEFAULT = new Builder();

		private String domain = null;
		private int maxAge = 0;
		private boolean httpOnly = false;
		private boolean partitioned = false;
		private String path = null;
		private String sameSite = null;
		private boolean secure = false;

		public Builder domain(String domain) {
			this.domain = domain;
			return this;
		}

		public Builder maxAge(int maxAge) {
			this.maxAge = maxAge;
			return this;
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
	}

	public HTTPResponseWithCookie(HTTPResponse original, String key, String value) {
		this(original, key, value, Builder.DEFAULT);
	}

	@Override
	public void build(HTTPResponseBuilder payload) throws Exception {
		var sb = new StringBuilder();
		sb.append(key).append("=").append(value);

		if (builder.domain != null) {
			sb.append("; Domain=").append(builder.domain);
		}

		if (builder.maxAge >= 0) {
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

		payload.setHeader("Set-Cookie", sb);
		original.build(payload);
	}
}
