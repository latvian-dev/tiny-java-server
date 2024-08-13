package dev.latvian.apps.tinyserver.error;

public class BindFailedException extends RuntimeException {
	public final int minPort;
	public final int maxPort;

	public BindFailedException(int minPort, int maxPort) {
		super(minPort == maxPort ? ("Failed to bind to port " + minPort) : ("Failed to bind to any port in range [" + minPort + ", " + maxPort + "]"));
		this.minPort = minPort;
		this.maxPort = maxPort;
	}
}
