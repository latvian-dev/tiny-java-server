package dev.latvian.apps.tinyhttp.content;

import dev.latvian.apps.tinyhttp.http.response.error.client.BadRequestError;
import dev.latvian.apps.tinyhttp.http.response.error.client.RangeNotSatisfiableError;

public record RequestRange(long start, long end) {
	public static RequestRange parse(String string, long length) {
		var numbers = string.split("-");

		if (numbers.length == 2) {
			numbers[0] = numbers[0].trim();
			numbers[1] = numbers[1].trim();

			var start = numbers[0].isEmpty() ? -1L : Long.parseLong(numbers[0]);
			var end = numbers[1].isEmpty() ? -1L : Long.parseLong(numbers[1]);

			if (start == -1L && end == -1L) {
				throw new BadRequestError("Both range bounds can't be empty");
			}

			if (start != -1L && end != -1L && start > end) {
				throw new BadRequestError("Range start > end");
			}

			if (start != -1L && start >= length) {
				throw new RangeNotSatisfiableError(length, "Range start >= length");
			}

			if (end != -1L && end >= length) {
				throw new RangeNotSatisfiableError(length, "Range end >= length");
			}

			if (start == -1L) {
				return new RequestRange(length - end, length - 1L);
			} else if (end == -1L) {
				return new RequestRange(start, length - 1L);
			} else {
				return new RequestRange(start, end);
			}
		}

		throw new BadRequestError("Invalid range: " + string);
	}

	public long length() {
		return end - start + 1L;
	}
}
