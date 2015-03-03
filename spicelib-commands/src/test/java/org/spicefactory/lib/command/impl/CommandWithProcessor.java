package org.spicefactory.lib.command.impl;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class CommandWithProcessor {

	private String result;

	public CommandWithProcessor(String result) {
		this.result = result;
	}

	public String execute() {
		return result;
	}

}
