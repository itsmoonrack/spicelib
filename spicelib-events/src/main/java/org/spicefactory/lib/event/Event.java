package org.spicefactory.lib.event;

import java.util.EventObject;

public class Event extends EventObject {

	private static final long serialVersionUID = 6873698011151396042L;

	public final int id;

	public Event(Object source, int id) {
		super(source);
		this.id = id;
	}

	/**
	 * Returns the event type.
	 */
	public int getID() {
		return id;
	}
}
