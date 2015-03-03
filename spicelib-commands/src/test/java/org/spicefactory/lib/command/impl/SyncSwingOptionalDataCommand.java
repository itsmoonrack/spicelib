package org.spicefactory.lib.command.impl;

import javax.annotation.Nullable;

import org.spicefactory.lib.command.model.CommandModel;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class SyncSwingOptionalDataCommand {

	public CommandModel model;

	public Object execute(@Nullable CommandModel param) {
		model = param;
		return model == null ? null : model.value;
	}

}
