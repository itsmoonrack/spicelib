package org.spicefactory.lib.command.base;

import org.spicefactory.lib.command.SuspendableCommand;
import org.spicefactory.lib.command.events.CommandEvent;

/**
 * Abstract base implementation of the SuspendableCommand interface.
 * <p>
 * A subclass of AbstractSuspendableCommand is expected to override the <code>doStart</code>, <code>doCancel</code>, <code>doSuspend</code> and
 * <code>doResume</code> methods and perform the necessary operations, and then call <code>complete</code> when the operation is done (or
 * <code>error</code> when the command fails to complete successfully).
 * </p>
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public abstract class AbstractSuspendableCommand extends AbstractCancellableCommand implements SuspendableCommand {

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	private boolean suspended;

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	public void suspend() {
		if (!isActive()) {
			logger.error("Attempt to suspend inactive command '{}'.", this);
			return;
		}
		if (isSuspended()) {
			logger.error("Attempt to suspend command '{}' which is already suspended.", this);
			return;
		}
		suspended = true;
		doSuspend();
		dispatchEvent(new CommandEvent(CommandEvent.SUSPEND));
	}

	@Override
	public void resume() {
		if (!isSuspended()) {
			logger.error("Attempt to resume command '{}' which is not suspended.", this);
			return;
		}
		suspended = false;
		doResume();
		dispatchEvent(new CommandEvent(CommandEvent.RESUME));
	}

	@Override
	public final boolean isSuspended() {
		return suspended;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Invoked when this command gets suspended.
	 * <p>
	 * Subclasses should override this method and suspend the actual operation this command performs.
	 * </p>
	 */
	protected abstract void doSuspend();

	/**
	 * Invoked when this command gets resumed.
	 * <p>
	 * Subclasses should override this method and resume the suspended operation this command performs.
	 * </p>
	 */
	protected abstract void doResume();
}
