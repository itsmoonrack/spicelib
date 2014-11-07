package org.spicefactory.lib.event;

public interface EventDispatcher<L extends EventListener<?>> {

	/**
	 * Registers an event listener object with an EventDispatcher object so that the listener receives notification of an event.
	 * <p>
	 * If you no longer need an event listener, remove it by calling removeEventListener(), or memory problems could result. Event listeners are
	 * not automatically removed from memory because the garbage collector does not remove the listener as long as the dispatching object exists.
	 * <p>
	 * Copying an EventDispatcher instance does not copy the event listeners attached to it. (If your newly created node needs an event listener,
	 * you must attach the listener after creating the node.) However, if you move an EventDispatcher instance, the event listeners attached to
	 * it move along with it.
	 * @param l The listener class that processes the event
	 */
	void addEventListener(int type, L l);

	/**
	 * Removes the specified event listener so it no longer receives events from this implementation.
	 * @param l
	 */
	void removeEventListener(int type, L l);

}