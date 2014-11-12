package org.spicefactory.lib.command.events;

import org.spicefactory.lib.command.CommandResult;

/**
 * Event dispatched by commands when they finished executing.
 * <p>
 * This event also implements the <code>CommandResult</code> interface.
 * @author Sylvain Lecoy <sylvain.lecoy@gmail.com>
 */
public class CommandResultEvent extends CommandEvent implements CommandResult {

	private static final long serialVersionUID = -383947540252804193L;

	/**
	 * Constant for the type of event fired when a command completed successfully.
	 */
	public static final int COMPLETE = 0x08;

	/**
	 * Constant for the type of event fired when a command aborted with an error.
	 */
	public static final int ERROR = 0x16;

	private final Object result;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	public CommandResultEvent(int id, Object result) {
		super(id);
		this.result = result;
	}

	@Override
	public Object value() {
		return result;
	}

	@Override
	public boolean complete() {
		return id == COMPLETE;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
