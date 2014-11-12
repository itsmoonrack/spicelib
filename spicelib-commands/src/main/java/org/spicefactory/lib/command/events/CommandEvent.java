package org.spicefactory.lib.command.events;

import org.spicefactory.lib.event.Event;

public class CommandEvent extends Event {

	private static final long serialVersionUID = 6907903111137648309L;

	/**
	 * Constant for the type of event fired when a command was cancelled.
	 */
	public static final int CANCEL = 0x01;

	/**
	 * Constant for the type of event fired when a command was suspended.
	 */
	public static final int SUSPEND = 0x02;

	/**
	 * Constant for the type of event fired when a command was resumed.
	 */
	public static final int RESUME = 0x04;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	public CommandEvent(int id) {
		super(id);
	}

	public Object command() {
		return source;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
