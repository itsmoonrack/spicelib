package org.spicefactory.lib.command.impl;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class SyncSwingResultCommand {

	public boolean executed;
	private Object result;

	public SyncSwingResultCommand(Object result) {
		this.result = result;
	}

	public Object execute() {
		executed = true;
		return result;
	}
}
