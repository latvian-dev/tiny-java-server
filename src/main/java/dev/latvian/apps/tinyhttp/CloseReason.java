package dev.latvian.apps.tinyhttp;

public record CloseReason(StatusCode status, boolean remote) {
}
