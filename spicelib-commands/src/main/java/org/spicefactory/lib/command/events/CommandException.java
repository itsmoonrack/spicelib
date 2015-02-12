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

	public Command getTarget() {
		return target;
	}

	public CommandExecutor getExecutor() {
		return executor;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
