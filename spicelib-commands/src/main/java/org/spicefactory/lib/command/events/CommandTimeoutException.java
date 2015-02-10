package org.spicefactory.lib.command.events;

import java.text.MessageFormat;

/**
 * Represents a timeout that occurred in a command.
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class CommandTimeoutException extends Exception {

	private final long timeout;

	public CommandTimeoutException(long timeout) {
		super(MessageFormat.format("A timeout occurred in a command: {0}.", timeout));
		this.timeout = timeout;
	}

	public long getTimeout() {
		return timeout;
	}
}
