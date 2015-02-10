package org.spicefactory.lib.command.base;

import org.spicefactory.lib.command.CommandResult;

/**
 * Default implementation of the CommandResult interface.
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class DefaultCommandResult implements CommandResult {

	private final Object command;
	private final Object value;
	private final boolean complete;

	private DefaultCommandResult(Object command) {
		this(command, null, true);
	}

	private DefaultCommandResult(Object command, Object value) {
		this(command, value, true);
	}

	private DefaultCommandResult(Object command, Object value, boolean complete) {
		this.command = command;
		this.value = value;
		this.complete = complete;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance for a command that successfully completed.
	 * @param command the command that completed its execution
	 * @param result the result produced by the command
	 */
	public static CommandResult forCompletion(Object command, Object result) {
		return new DefaultCommandResult(command, result);
	}

	/**
	 * Creates a new instance for a command that failed to execute.
	 * @param command the command that failed to execute
	 * @param cause the cause of the exception
	 */
	public static CommandResult forException(Object command, Throwable cause) {
		return new DefaultCommandResult(command, cause, false);
	}

	/**
	 * Creates a new instance for a command that has been cancelled.
	 * @param command the command that has been cancelled
	 */
	public static CommandResult forCancellation(Object command) {
		return new DefaultCommandResult(command, null, false);
	}

	@Override
	public Object command() {
		return command;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public boolean complete() {
		return complete;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
