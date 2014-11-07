package org.spicefactory.lib.event;

import java.util.EventObject;

public class Event extends EventObject {

	private static final long serialVersionUID = 6873698011151396042L;

	public final int id;

	public Event(int id) {
		super(/* Dummy Object */new Object());
		this.id = id;
	}

	/**
	 * Sets the source.
	 * @param source
	 */
	// Package-protected.
	void setSource(Object source) {
		this.source = source;
	}

	/**
	 * Returns the event type.
	 */
	public int getID() {
		return id;
	}
}
