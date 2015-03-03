package org.spicefactory.lib.command.impl;

import org.spicefactory.lib.command.model.CommandModel;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class SyncSwingConstructorInjectionCommand {

	public final CommandModel model;
	public final String string;

	public SyncSwingConstructorInjectionCommand(CommandModel model) {
		this(model, "");
	}

	public SyncSwingConstructorInjectionCommand(CommandModel model, String string) {
		this.model = model;
		this.string = string;
	}

	public Object execute() {
		return model.value + ":" + string;
	}

}
