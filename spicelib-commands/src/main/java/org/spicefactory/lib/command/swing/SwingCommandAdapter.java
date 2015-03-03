package org.spicefactory.lib.command.swing;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.spicefactory.lib.command.CommandResult;
import org.spicefactory.lib.command.adapter.CommandAdapter;
import org.spicefactory.lib.command.base.AbstractSuspendableCommand;
import org.spicefactory.lib.command.base.DefaultCommandResult;
import org.spicefactory.lib.command.callback.Callback;
import org.spicefactory.lib.command.data.CommandData;
import org.spicefactory.lib.command.data.DefaultCommandData;
import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.command.events.CommandException;
import org.spicefactory.lib.command.lifecycle.CommandLifecycle;

class SwingCommandAdapter extends AbstractSuspendableCommand implements CommandAdapter {

	private CommandLifecycle lifecycle;
	private DefaultCommandData data = new DefaultCommandData();

	private final Object target;
	private final Field callbackField;
	private final Method executeMethod;
	private final Method cancelMethod;
	private final Method resultMethod;
	private final Method exceptionMethod;
	private final boolean async;
	private final SwingCommand command;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	SwingCommandAdapter(Object target, Method execute, Field callback, Method cancel, Method result, Method error, boolean async) {
		this.target = target;
		this.callbackField = callback;
		this.executeMethod = execute;
		this.cancelMethod = cancel;
		this.resultMethod = result;
		this.exceptionMethod = error;
		this.async = async;
		this.command = async ? new SwingCommand() : null;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	public void prepare(CommandLifecycle lifecycle, CommandData data) {
		this.lifecycle = lifecycle;
		this.data = new DefaultCommandData(data);
	}

	@Override
	public boolean isCancellable() {
		return async;
	}

	@Override
	public boolean isSuspendable() {
		return false;
	}

	@Override
	public Object getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + target + ")";
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	protected void doSuspend() {
		throw new IllegalAccessError("Command is not suspendable.");
	}

	@Override
	protected void doResume() {
		throw new IllegalAccessError("Command cannot be resumed.");
	}

	@Override
	protected void doCancel() {
		if (command != null) {
			command.cancel(true);
		} else {
			try {
				cancelMethod.invoke(target);
			}
			catch (Exception e) {
				throw new Error(e);
			}
		}
		afterCompletion(DefaultCommandResult.forCancellation(this));
	}

	@Override
	protected void doExecute() {
		lifecycle.beforeExecution(target, data);

		try {
			if (callbackField != null) {
				callbackField.set(target, callback);
			}
		}
		catch (Exception e) {
			// Nothing we can do.
		}

		try {
			if (async) {
				command.execute();
			} else {
				// Result can be null if invoked method return type is void.
				Object result = executeMethod.invoke(target, getParameters());
				handleResult(result);
			}
		}
		catch (InvocationTargetException e) {
			afterCompletion(DefaultCommandResult.forException(target, e.getCause()));
			exception(e.getCause());
		}
		catch (Exception e) {
			throw new Error(e);
		}
	}

	private Object[] getParameters() {
		Class<?>[] parameterTypes = executeMethod.getParameterTypes();
		Object[] parameters = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; ++i) {
			if (parameterTypes[i].isAssignableFrom(Callback.class)) {
				parameters[i] = callback;
				continue;
			}
			parameters[i] = data.getObject(parameterTypes[i]);
		}
		return parameters;
	}

	private void afterCompletion(CommandResult result) {
		lifecycle.afterCompletion(target, result);
	}

	private void handleResult(Object result) {
		result = invokeResultHandler(resultMethod, result);
		if (result instanceof Throwable) {
			handleException((Throwable) result);
			return;
		}
		afterCompletion(DefaultCommandResult.forCompletion(target, result));
		complete(result);
	}

	private void handleException(Throwable cause) {
		cause = (Throwable) invokeResultHandler(exceptionMethod, cause);
		afterCompletion(DefaultCommandResult.forException(target, cause));
		exception(cause);
	}

	private Object invokeResultHandler(Method method, Object value) {
		if (method == null)
			return value;

		Object param = getParam(method, value);
		try {
			if (method.getReturnType().isAssignableFrom(Void.class)) {
				method.invoke(target, param);
				return value;
			} else {
				return method.invoke(target, param);
			}
		}
		catch (Throwable t) {
			return t;
		}
	}

	private Object getParam(Method method, Object value) {
		if (value instanceof CommandException) {
			if (!(method.getParameterTypes()[0].isAssignableFrom(CommandException.class))) {
				return ((CommandException) value).getCause();
			}
		}
		return value;
	}

	private void handleCancellation() {
		// do not call cancel to bypass doCancel
		afterCompletion(DefaultCommandResult.forCancellation(target));
		dispatchEvent(new CommandEvent(CommandEvent.CANCEL));
	}

	private class SwingCommand extends SwingWorker<Object, Void> {

		@Override
		protected Object doInBackground() throws Exception {
			return executeMethod.invoke(target, getParameters());
		}

		@Override
		protected void done() {
			//			try {
			//				handleResult(get());
			//			}
			//			catch (InterruptedException e) {
			//				handleCancellation();
			//			}
			//			catch (ExecutionException e) {
			//				handleException(e.getCause());
			//			}
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// Pre-Java 8 implementation.
	/////////////////////////////////////////////////////////////////////////////

	private final Callback<Object> callback = new Callback<Object>() {
		@Override
		public void result(final Object result) {
			if (!isActive()) {
				throw new IllegalStateException("Callback invoked although command " + target + " is not active");
			}
			if (SwingUtilities.isEventDispatchThread()) {
				handleResult(result);
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						handleResult(result);
					}
				});
			}
		}

		@Override
		public void exception(final Throwable result) {
			if (!isActive()) {
				throw new IllegalStateException("Callback invoked although command " + target + " is not active");
			}
			if (SwingUtilities.isEventDispatchThread()) {
				handleException(result);
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						handleException(result);
					}
				});
			}
		}

		@Override
		public void cancel() {
			if (!isActive()) {
				throw new IllegalStateException("Callback invoked although command " + target + " is not active");
			}
			if (SwingUtilities.isEventDispatchThread()) {
				handleCancellation();
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						handleCancellation();
					}
				});
			}
		}
	};

}
