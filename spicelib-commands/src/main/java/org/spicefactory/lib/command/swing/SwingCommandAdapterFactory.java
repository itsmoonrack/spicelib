package org.spicefactory.lib.command.swing;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.spicefactory.lib.command.adapter.CommandAdapter;
import org.spicefactory.lib.command.adapter.CommandAdapterFactory;
import org.spicefactory.lib.command.callback.Callback;

/**
 * A CommandAdapterFactory implementation that creates adapters from commands that adhere to the conventions of Spicelib's "Swing Commands".
 * @author Sylvain Lecoy <sylvain.lecoy@gmail.com>
 */
public class SwingCommandAdapterFactory implements CommandAdapterFactory {

	@Override
	public CommandAdapter createAdapter(Object instance) {
		Method execute = null;
		Method cancel = null;
		Method result = null;
		Method error = null;

		for (Method m : instance.getClass().getMethods()) {
			if ("execute".equals(m.getName())) {
				execute = m;
			}
			if ("cancel".equals(m.getName()) && m.getParameterTypes().length == 0) {
				cancel = m;
			}
			if ("result".equals(m.getName()) && m.getParameterTypes().length == 1) {
				result = m;
			}
			if ("error".equals(m.getName()) && m.getParameterTypes().length == 1) {
				error = m;
			}
		}

		if (execute == null)
			return null;

		boolean async = false;

		for (Class<?> param : execute.getParameterTypes()) {
			if (param.isAssignableFrom(Callback.class)) {
				async = true;
				break;
			}
		}

		Field callback = null;

		try {
			callback = instance.getClass().getField("callback");
			if (callback.getType().isAssignableFrom(Callback.class)) {
				async = true;
			} else {
				callback = null;
			}
		}
		catch (Exception e) {
			// Nothing to do.
		}

		return new SwingCommandAdapter(instance, execute, callback, cancel, result, error, async);
	}
}
