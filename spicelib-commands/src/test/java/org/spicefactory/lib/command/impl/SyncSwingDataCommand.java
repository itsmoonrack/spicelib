package org.spicefactory.lib.command.impl;

import javax.annotation.Nullable;

import org.spicefactory.lib.command.model.CommandModel;
import org.spicefactory.lib.command.model.FlowModel;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class SyncSwingDataCommand {

	public CommandModel model;
	private boolean throwException;

	public SyncSwingDataCommand() {
		this(false);
	}

	public SyncSwingDataCommand(boolean throwException) {
		this.throwException = throwException;
	}

	public Object execute(CommandModel param, @Nullable FlowModel flow) {
		model = param;
		if (throwException) {
			throw new IllegalStateException("Sorry, I was told to throw an Exception.");
		}
		if (flow != null) {
			flow.addCommand(model.value.toString());
		}
		return model.value;
	}

}
