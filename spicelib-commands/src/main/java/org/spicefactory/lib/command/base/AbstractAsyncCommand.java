package org.spicefactory.lib.command.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spicefactory.lib.command.AsyncCommand;
import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.command.events.CommandResultEvent;
import org.spicefactory.lib.event.EventDispatcher;
import org.spicefactory.lib.event.EventListener;

/**
 * Abstract base implementation of the AsyncCommand interface.
 * <p>
 * A subclass of AbstractAsyncCommand is expected to override the <code>doStart</code> method, do its work and then call <code>complete</code>
 * when the operation is done (or <code>error</code> when the command fails to complete successfully).
 * </p>
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public abstract class AbstractAsyncCommand extends EventDispatcher<EventListener<CommandEvent>, CommandEvent> implements AsyncCommand {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private boolean active;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance.
	 */
	protected AbstractAsyncCommand() {
		//		Java 8:
		//		addEventListener(CommandEvent.CANCEL, e -> active = false);
		addEventListener(CommandEvent.CANCEL, handleCancellation);
	}

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Starts the execution of this command. If this command is member of a group or a flow this method should not be called by application code.
	 */
	@Override
	public void execute() {
		if (isActive()) {
			logger.error("Attempt to execute a command '{}' which is already active.", this);
			return;
		}
		active = true;
		doExecute();
	}

	@Override
	public final boolean isActive() {
		return active;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Signals that this command has completed. Subclasses should call this method when the asynchronous operation has completed.
	 */
	protected void complete() {
		complete(null);
	}

	/**
	 * Signals that this command has completed. Subclasses should call this method when the asynchronous operation has completed.
	 */
	protected final void complete(Object result) {
		if (!isActive()) {
			logger.error("Attempt to complete command '{}' although it is not active.", this);
			return;
		}
		active = false;
		dispatchEvent(new CommandResultEvent(CommandResultEvent.COMPLETE, result));
	}

	/**
	 * Signals an error condition and cancels the command. Subclasses should call this method when the asynchronous operation cannot be
	 * successfully completed.
	 */
	protected void exception() {
		exception(null);
	}

	/**
	 * Signals an exception condition and cancels the command. Subclasses should call this method when the asynchronous operation cannot be
	 * successfully completed.
	 */
	protected final void exception(Object result) {
		if (!isActive()) {
			logger.error("Attempt to dispatch error event for command '{}' although it is not active.", this);
			return;
		}
		active = false;
		dispatchEvent(new CommandResultEvent(CommandResultEvent.EXCEPTION, result));
	}

	/**
	 * Invoked when the command starts executing.
	 * <p>
	 * Subclasses should override this method to start with the actual operation this command performs.
	 * </p>
	 */
	protected abstract void doExecute();

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	private final EventListener<CommandEvent> handleCancellation = new EventListener<CommandEvent>() {
		@Override
		public void process(CommandEvent event) {
			active = false;
		}
	};
}
