package org.spicefactory.lib.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * This class represents an event dispatcher object, or "data" in the model-view paradigm. It can be sub-classed to represent an object that the
 * application wants to have observed.
 * @author Sylvain Lecoy <sylvain.lecoy@gmail.com>
 * @param <L>
 * @param <E>
 */
public abstract class EventDispatcher<L extends EventListener<E>, E extends Event> implements IEventDispatcher<L> {

	private final Object source;
	private final Map<Integer, Vector<L>> listenersByType = new HashMap<Integer, Vector<L>>();

	// Used when extending this class.
	protected EventDispatcher() {
		this.source = this;
	}

	// Used when composing this class so the source object is not this support but the actual dispatcher.
	protected EventDispatcher(Object source) {
		this.source = source;
	}

	/* (non-Javadoc)
	 * @see org.spicefactory.parsley.core.events.EventDispatcher#addEventListener(L)
	 */
	@Override
	public synchronized void addEventListener(int type, L l) {
		if (l == null) {
			throw new NullPointerException();
		}

		Vector<L> listeners = listenersByType.get(type);

		if (listeners == null) {
			listeners = new Vector<L>();
			listenersByType.put(type, listeners);
		}

		listeners.addElement(l);
	}

	/* (non-Javadoc)
	 * @see org.spicefactory.parsley.core.events.EventDispatcher#removeEventListener(L)
	 */
	@Override
	public synchronized void removeEventListener(int type, L l) {
		Vector<L> listeners = listenersByType.get(type);

		if (listeners == null) {
			return;
		}

		listeners.removeElement(l);
	}

	@SuppressWarnings("unchecked")
	public void dispatchEvent(E e) {
		Vector<L> listeners;
		// Sets the source object to this event.
		e.setSource(source);

		if (listenersByType.containsKey(e.getID())) {
			listeners = listenersByType.get(e.getID());
			Object[] arrLocal = new Object[listeners.size()];

			synchronized (this) {
				arrLocal = listeners.toArray(arrLocal);
			}

			for (int i = 0; i < arrLocal.length; i++) {
				((L) arrLocal[i]).process(e);
			}
		}
	}
}
