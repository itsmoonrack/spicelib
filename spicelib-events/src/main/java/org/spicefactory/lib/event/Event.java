package org.spicefactory.lib.event;

import java.util.EventObject;

public class Event extends EventObject {

	private static final long serialVersionUID = 6873698011151396042L;

	public final int id;

<<<<<<< HEAD
	public Event(int id) {
		super(/* Dummy Object */new Object());
=======
	public Event(Object source, int id) {
		super(source);
>>>>>>> 412b0bf9089695734842bd015015227b1c3ab71a
		this.id = id;
	}

	/**
<<<<<<< HEAD
	 * Sets the source.
	 * @param source
	 */
	// Package-protected.
	void setSource(Object source) {
		this.source = source;
	}

	/**
=======
>>>>>>> 412b0bf9089695734842bd015015227b1c3ab71a
	 * Returns the event type.
	 */
	public int getID() {
		return id;
	}
}
