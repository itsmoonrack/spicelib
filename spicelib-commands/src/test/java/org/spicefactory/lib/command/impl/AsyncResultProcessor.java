package org.spicefactory.lib.command.impl;

import org.spicefactory.lib.command.callback.Callback;
import org.spicefactory.lib.command.callback.ExceptionCallback;
import org.spicefactory.lib.command.callback.ResultCallback;
import org.spicefactory.lib.command.model.AsyncResult;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class AsyncResultProcessor {

	private static int cancellations;
	private Callback<Object> callback;

	public void execute(AsyncResult async, Callback<Object> callback) {
		this.callback = callback;
		async.addHandler(result, exception);
	}

	private ResultCallback<Object> result = new ResultCallback<Object>() {
		@Override
		public void result(Object result) {
			callback.result(result);
		}
	};

	private ExceptionCallback<Throwable> exception = new ExceptionCallback<Throwable>() {
		@Override
		public void exception(Throwable cause) {
			callback.exception(new Exception("Expected Error", cause));
		}
	};

	public void cancel() {
		cancellations++;
	}

}
