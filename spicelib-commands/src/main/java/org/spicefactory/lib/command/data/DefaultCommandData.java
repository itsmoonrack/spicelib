package org.spicefactory.lib.command.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of the CommandData interface.
 * @author Sylvain Lecoy <sylvain.lecoy@gmail.com>
 */
public class DefaultCommandData implements CommandData {

	private final List<Object> data = new LinkedList<Object>();
	private final CommandData parent;

	private volatile boolean inProgress; // TODO: Check the purpose of inProgress, concurrency ?

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance.
	 * @param parent the parent to look up results not found in this instance
	 */
	public DefaultCommandData() {
		this(null);
	}

	/**
	 * Creates a new instance.
	 * @param parent the parent to look up results not found in this instance
	 */
	public DefaultCommandData(CommandData parent) {
		this.parent = parent;
	}

	/**
	 * Adds a value to this instance.
	 * <p>
	 * This method is usually only invoked by a command executor that adds the result by one of its commands to this instance.
	 * </p>
	 * @param value the value to add to this instance
	 */
	public void addValue(Object value) {
		data.add(value);
	}

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	public Object getObject() {
		return getObject(Object.class);
	}

	public Object getObject(Class<?> type) {
		if (inProgress) {
			return null;
		}

		try {
			inProgress = true;
			for (Object value : data) {
				if (value.getClass().isAssignableFrom(type)) {
					return value;
				} else if (value instanceof CommandData) {
					Object result = ((CommandData) value).getObject(type);
					if (result != null) {
						return result;
					}
				}
			}
			return parent != null ? parent.getObject(type) : null;
		}
		finally {
			inProgress = false;
		}
	}

	public List<Object> getObjects() {
		return getObjects(Object.class);
	}

	public List<Object> getObjects(Class<?> type) {
		if (inProgress) {
			return new ArrayList<Object>(0);
		}

		List<Object> results = new LinkedList<Object>();
		try {
			inProgress = true;
			for (Object value : data) {
				if (value instanceof CommandData) {
					results.addAll(((CommandData) value).getObjects(type));
				} else if (value.getClass().isAssignableFrom(type)) {
					results.add(value);
				}
			}
			if (parent != null) {
				results.addAll(parent.getObjects(type));
			}
		}
		finally {
			inProgress = false;
		}
		return results;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
