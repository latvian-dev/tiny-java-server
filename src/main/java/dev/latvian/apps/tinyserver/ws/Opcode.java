package dev.latvian.apps.tinyserver.ws;

public enum Opcode {
	CONTINUOUS(0),
	TEXT(1),
	BINARY(2),

	CLOSING(8),
	PING(9),
	PONG(10),

	;

	public static Opcode get(int opcode) {
		return switch (opcode) {
			case 0 -> CONTINUOUS;
			case 1 -> TEXT;
			case 2 -> BINARY;
			case 8 -> CLOSING;
			case 9 -> PING;
			case 10 -> PONG;
			default -> throw new IllegalArgumentException("Invalid opcode: " + opcode);
		};
	}

	public final byte opcode;

	Opcode(int opcode) {
		this.opcode = (byte) opcode;
	}
}