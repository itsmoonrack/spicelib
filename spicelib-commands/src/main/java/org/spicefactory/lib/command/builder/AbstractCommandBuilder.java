package org.spicefactory.lib.command.builder;

import org.spicefactory.lib.command.Command;
import org.spicefactory.lib.command.proxy.CommandProxy;

public abstract class AbstractCommandBuilder implements CommandBuilder {

	private final CommandProxy proxy;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	protected AbstractCommandBuilder(CommandProxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * Sets the target command to execute.
	 * @param target the target command to execute
	 */
	protected void setTarget(Command target) {
	}

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	public CommandProxy execute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandProxy build() {
		// TODO Auto-generated method stub
		return null;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
