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
public abstract class AbstractEventDispatcher<L extends EventListener<E>, E extends Event> implements EventDispatcher<L> {

	private final Map<Integer, Vector<L>> listenersByType = new HashMap<Integer, Vector<L>>();

	/* (non-Javadoc)
	 * @see org.spicefactory.parsley.core.events.EventDispatcher#addEventListener(L)
	 */
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
	public synchronized void removeEventListener(int type, L l) {
		Vector<L> listeners = listenersByType.get(type);

		if (listeners == null) {
			return;
		}

		listeners.removeElement(l);
	}

	@SuppressWarnings("unchecked")
	protected void dispatchEvent(E e) {
		Vector<L> listeners;

		if (listenersByType.containsKey(e.getID())) {
			listeners = listenersByType.get(e.getID());
			Object[] arrLocal = new Object[listeners.size()];

			synchronized (this) {
				arrLocal = listeners.toArray(arrLocal);
			}

			for (int i = arrLocal.length - 1; i > 0; i--) {
				((L) arrLocal[i]).process(e);
			}
		}
	}

}
