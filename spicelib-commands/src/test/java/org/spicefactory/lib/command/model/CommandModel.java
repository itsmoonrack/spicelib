package org.spicefactory.lib.command.model;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class CommandModel {

	public final Object value;
	private boolean injected;

	public CommandModel(Object value) {
		this.value = value;
	}

	public void markAsInjected() {
		injected = true;
	}

	public boolean isInjected() {
		return injected;
	}

}
