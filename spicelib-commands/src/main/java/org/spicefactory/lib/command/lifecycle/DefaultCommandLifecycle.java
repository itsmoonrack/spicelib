package org.spicefactory.lib.command.lifecycle;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.spicefactory.lib.command.CommandResult;
import org.spicefactory.lib.command.data.CommandData;

public class DefaultCommandLifecycle implements CommandLifecycle {

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	public <T> T createInstance(Class<T> type, CommandData data) {
		try {
			Iterator<Constructor<T>> constructors = getDeclaredConstructorInOrder(type);
			while (constructors.hasNext()) {
				Constructor<T> constructor = constructors.next();
				List<Object> params = new ArrayList<Object>();
				for (Class<?> param : constructor.getParameterTypes()) {
					Object value = data.getObject(param);
					if (value != null) {
						params.add(value);
					} else if (!constructors.hasNext()) {
						throw new IllegalStateException("No data available for required constructor parameter of type " + param);
					} else {
						break;
					}
				}
				if (params.size() == constructor.getParameterTypes().length) {
					return constructor.newInstance(params.toArray());
				}
			}
			return type.newInstance();
		}
		catch (IllegalStateException e) {
			throw e;
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public void beforeExecution(Object command, CommandData data) {
		/* Default implementation does nothing. */
	}

	@Override
	public void afterCompletion(Object command, CommandResult result) {
		/* Default implementation does nothing. */
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	private <T> Iterator<Constructor<T>> getDeclaredConstructorInOrder(Class<T> type) {
		final List<ConstructorRegistration> reg = new ArrayList<ConstructorRegistration>(type.getDeclaredConstructors().length);
		for (Constructor<?> c : type.getDeclaredConstructors()) {
			reg.add(new ConstructorRegistration(c));
		}
		Collections.sort(reg);
		final List<Constructor<T>> constructors = new ArrayList<Constructor<T>>(reg.size());
		for (ConstructorRegistration r : reg) {
			constructors.add((Constructor<T>) r.constructor);
		}
		return constructors.iterator();
	}

	private static class ConstructorRegistration implements Comparable<ConstructorRegistration> {

		private final int order;
		private final Constructor<?> constructor;

		public ConstructorRegistration(Constructor<?> constructor) {
			this.order = constructor.getParameterTypes().length;
			this.constructor = constructor;
		}

		@Override
		public int compareTo(ConstructorRegistration o) {
			return o.order - order; // DESC
		}

	}

}
