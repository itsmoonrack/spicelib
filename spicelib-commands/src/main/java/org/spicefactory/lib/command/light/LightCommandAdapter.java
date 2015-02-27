package org.spicefactory.lib.command.light;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.spicefactory.lib.command.CommandResult;
import org.spicefactory.lib.command.adapter.CommandAdapter;
import org.spicefactory.lib.command.base.AbstractSuspendableCommand;
import org.spicefactory.lib.command.base.DefaultCommandResult;
import org.spicefactory.lib.command.builder.CommandProxyBuilder;
import org.spicefactory.lib.command.callback.Callback;
import org.spicefactory.lib.command.callback.CancelCallback;
import org.spicefactory.lib.command.callback.ExceptionCallback;
import org.spicefactory.lib.command.callback.ResultCallback;
import org.spicefactory.lib.command.data.CommandData;
import org.spicefactory.lib.command.data.DefaultCommandData;
import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.command.events.CommandException;
import org.spicefactory.lib.command.lifecycle.CommandLifecycle;
import org.spicefactory.lib.command.proxy.CommandProxy;
import org.spicefactory.lib.command.result.ResultProcessors;

class LightCommandAdapter extends AbstractSuspendableCommand implements CommandAdapter {

	private CommandLifecycle lifecycle;
	private CommandProxy resultProcessor;
	private DefaultCommandData data = new DefaultCommandData();

	private final Object target;
	private final Field callbackField;
	private final Method executeMethod;
	private final Method cancelMethod;
	private final Method resultMethod;
	private final Method exceptionMethod;
	private final boolean async;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	public LightCommandAdapter(Object target, Method execute, Field callback, Method cancel, Method result, Method error, boolean async) {
		this.target = target;
		this.callbackField = callback;
		this.executeMethod = execute;
		this.cancelMethod = cancel;
		this.resultMethod = result;
		this.exceptionMethod = error;
		this.async = async;
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
		if (resultProcessor != null) {
			resultProcessor.cancel();
			resultProcessor = null;
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
				executeMethod.invoke(target, getParameters());
			} else {
				// Result can be null if invoked method return type is void.
				Object result = executeMethod.invoke(target, getParameters());
				handleResult(result);
			}
		}
		catch (Exception e) {
			afterCompletion(DefaultCommandResult.forException(target, e));
			exception(e);
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
		CommandProxyBuilder builder = result == null ? null : ResultProcessors.newProcessor(target, result);
		if (builder != null) {
			processResult(builder);
		} else {
			handleCompletion(result);
		}
	}

	private void handleCompletion(Object result) {
		result = invokeResultHandler(resultMethod, result);
		if (result instanceof Throwable) {
			handleException((Throwable) result);
			return;
		}
		afterCompletion(DefaultCommandResult.forCompletion(target, result));
		resultProcessor = null;
		complete(result);
	}

	private void handleException(Throwable cause) {
		cause = (Throwable) invokeResultHandler(exceptionMethod, cause);
		afterCompletion(DefaultCommandResult.forException(target, cause));
		resultProcessor = null;
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
		resultProcessor = null;
		dispatchEvent(new CommandEvent(CommandEvent.CANCEL));
	}

	private void processResult(CommandProxyBuilder builder) {
		resultProcessor = builder //
				.result(commandCompletionCallback) //
				.exception(commandExceptionCallback) //
				.cancel(commandCancellationCallback) //
				.execute();
	}

	/////////////////////////////////////////////////////////////////////////////
	// Pre-Java 8 implementation.
	/////////////////////////////////////////////////////////////////////////////

	private final Callback<Object> callback = new Callback<Object>() {
		@Override
		public void result(Object result) {
			if (!isActive()) {
				throw new IllegalStateException("Callback invoked although command " + target + " is not active");
			}
			handleResult(result);
		}

		@Override
		public void exception(Throwable result) {
			if (!isActive()) {
				throw new IllegalStateException("Callback invoked although command " + target + " is not active");
			}
			handleException(result);
		}

		@Override
		public void cancel() {
			if (!isActive()) {
				throw new IllegalStateException("Callback invoked although command " + target + " is not active");
			}
			handleCancellation();
		}
	};

	private final ResultCallback<Object> commandCompletionCallback = new ResultCallback<Object>() {

		@Override
		public void result(Object result) {
			handleCompletion(result);
		}

	};

	private final ExceptionCallback<Throwable> commandExceptionCallback = new ExceptionCallback<Throwable>() {

		@Override
		public void exception(Throwable cause) {
			handleException(cause);
		}

	};

	private final CancelCallback commandCancellationCallback = new CancelCallback() {

		@Override
		public void cancel() {
			handleCancellation();
		}

	};

}
