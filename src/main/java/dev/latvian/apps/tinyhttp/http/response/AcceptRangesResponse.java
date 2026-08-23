package dev.latvian.apps.tinyhttp.http.response;

public record AcceptRangesResponse(HTTPResponse original, String ranges) implements ChainedHTTPResponse {
	@Override
	public void build(HTTPPayload payload) {
		payload.setAcceptRanges(ranges);
		original.build(payload);
	}
}
