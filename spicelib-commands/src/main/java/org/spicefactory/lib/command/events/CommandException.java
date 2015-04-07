package org.spicefactory.lib.command.events;

import java.text.MessageFormat;

import org.spicefactory.lib.command.Command;
import org.spicefactory.lib.command.CommandExecutor;

public class CommandException extends Exception {

	private final Command target;
	private final CommandExecutor executor;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	public CommandException(CommandExecutor executor, Command target, Throwable cause) {
		super(MessageFormat.format("Execution of {0} failed, target command {1} failed.", executor, target), cause);
		this.executor = executor;
		this.target = target;
	}

	public CommandException(CommandExecutor executor, Command target, Object exception) {
		this(executor, target, exception instanceof Throwable ? (Throwable) exception : new ObjectException(exception));
	}

	public Command getTarget() {
		return target;
	}

	public CommandExecutor getExecutor() {
		return executor;
	}

	/**
	 * Use this to get the cause of the exception.
	 * <p>
	 * Might not necessary be a Throwable in case of framework exception.
	 * @return
	 */
	public Object getException() {
		Throwable throwable = getCause();
		if (throwable instanceof ObjectException) {
			return ((ObjectException) throwable).exception;
		}
		return throwable;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	private static class ObjectException extends Throwable {

		private final Object exception;

		public ObjectException(Object exception) {
			this.exception = exception;
		}

	}
}
