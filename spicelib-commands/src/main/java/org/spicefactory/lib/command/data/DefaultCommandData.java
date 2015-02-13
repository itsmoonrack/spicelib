package org.spicefactory.lib.command.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of the CommandData interface.
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class DefaultCommandData implements CommandData {

	private final List<Object> data = new ArrayList<Object>();
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

	@Override
	public Object getObject() {
		return getObject(Object.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getObject(Class<T> type) {
		if (inProgress) {
			return null;
		}

		try {
			inProgress = true;
			for (int i = data.size() - 1; i >= 0; i--) {
				Object value = data.get(i);
				if (type.isAssignableFrom(value.getClass())) {
					return (T) value;
				} else if (value instanceof CommandData) {
					Object result = ((CommandData) value).getObject(type);
					if (result != null) {
						return (T) result;
					}
				}
			}
			return parent != null ? parent.getObject(type) : null;
		}
		finally {
			inProgress = false;
		}
	}

	@Override
	public List<Object> getObjects() {
		return getObjects(Object.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getObjects(Class<T> type) {
		if (inProgress) {
			return new ArrayList<T>(0);
		}

		List<T> results = new LinkedList<T>();
		try {
			inProgress = true;
			for (Object value : data) {
				if (value instanceof CommandData) {
					results.addAll(((CommandData) value).getObjects(type));
				} else if (type.isAssignableFrom(value.getClass())) {
					results.add((T) value);
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
