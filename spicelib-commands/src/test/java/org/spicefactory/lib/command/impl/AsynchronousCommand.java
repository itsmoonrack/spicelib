package org.spicefactory.lib.command.impl;

import org.spicefactory.lib.command.base.AbstractAsyncCommand;
import org.spicefactory.lib.command.model.CommandModel;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class AsynchronousCommand extends AbstractAsyncCommand {

	public int executions = 0;
	public int completions = 0;
	public int exceptions = 0;

	public final Object injection;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	public AsynchronousCommand() {
		this.injection = null;
	}

	public AsynchronousCommand(CommandModel optionalInjection) {
		this.injection = optionalInjection.value;
		optionalInjection.markAsInjected();
	}

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	public void forceCompletion() {
		forceCompletion(null);
	}

	public void forceCompletion(Object result) {
		completions++;
		complete(result);
	}

	public void forceException() {
		forceCompletion(null);
	}

	public void forceException(Throwable cause) {
		exceptions++;
		exception(cause);
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	protected void doExecute() {
		executions++;
	}
}
