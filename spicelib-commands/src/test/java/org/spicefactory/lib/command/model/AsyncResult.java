package org.spicefactory.lib.command.model;

import org.spicefactory.lib.command.callback.ExceptionCallback;
import org.spicefactory.lib.command.callback.ResultCallback;

public class AsyncResult {

	private ResultCallback<Object> complete;
	private ExceptionCallback<Throwable> exception;

	public void addHandler(ResultCallback<Object> result, ExceptionCallback<Throwable> exception) {
		this.complete = result;
		this.exception = exception;
	}

	public void invokeCompleteHandler(Object result) {
		complete.result(result);
	}

	public void invokeExceptionHandler(Throwable cause) {
		exception.exception(cause);
	}

	public void cancel() {
		complete.result(null);
	}

}
