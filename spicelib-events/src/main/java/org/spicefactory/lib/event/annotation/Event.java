package org.spicefactory.lib.event.annotation;

public @interface Event {

	/**
	 * Specifies the name of the event.
	 */
	int id();

	/**
	 * Specifies the data type of the event object. It is either the base event class, Event, or a subclass of the Event class.
	 */
	Class<? extends org.spicefactory.lib.event.Event> type();

}
