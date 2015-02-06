package org.spicefactory.lib.command.base;

import org.spicefactory.lib.command.CancellableCommand;
import org.spicefactory.lib.command.events.CommandEvent;

/**
 * Abstract base implementation of the CancellableCommand interface.
 * <p>
 * A subclass of AbstractCancellableCommand is expected to override the code <code>doStart</code> method, do its work and then call
 * <code>complete</code> when the operation is done (or <code>error</code> when the command fails to complete successfully). It is also expected
 * to override the <code>doCancel</code> method to cancel the actual operation.
 * </p>
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public abstract class AbstractCancellableCommand extends AbstractAsyncCommand implements CancellableCommand {

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	public void cancel() {
		if (!isActive()) {
			logger.error("Attempt to suspend inactive command '{}'", this);
			return;
		}
		doCancel();
		dispatchEvent(new CommandEvent(CommandEvent.CANCEL));
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Invoked when this command gets cancelled.
	 * <p>
	 * Subclasses should override this method and cancel the actual operation this command performs.
	 * </p>
	 */
	protected abstract void doCancel();
}
