package org.spicefactory.lib.command.impl;

import org.spicefactory.lib.command.Command;

public class SynchronousCommand implements Command {

	public static int totalExecutions = 0;

	public int executions = 0;
	public boolean throwException = false;

	public SynchronousCommand() {
		this(false);
	}

	public SynchronousCommand(boolean throwException) {
		this.throwException = throwException;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	public static void resetTotalExecutions() {
		totalExecutions = 0;
	}

	@Override
	public void execute() {
		executions++;
		totalExecutions++;
		if (throwException)
			throw new RuntimeException("This is expected");
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
