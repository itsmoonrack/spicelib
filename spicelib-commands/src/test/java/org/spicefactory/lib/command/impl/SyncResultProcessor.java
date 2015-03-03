package org.spicefactory.lib.command.impl;

import org.spicefactory.lib.command.callback.Callback;
import org.spicefactory.lib.command.model.CommandModel;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class SyncResultProcessor {

	public void execute(Object result, Callback<Object> callback) {
		callback.result(new CommandModel(result));
	}

}
