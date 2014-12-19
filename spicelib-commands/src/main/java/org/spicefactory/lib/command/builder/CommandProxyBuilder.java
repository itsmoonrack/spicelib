package org.spicefactory.lib.command.builder;

import org.spicefactory.lib.command.proxy.CommandProxy;

/**
 * A builder DSL for creating CommandProxy instances, responsible for executing a single command.
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class CommandProxyBuilder extends AbstractCommandBuilder {

	private final Object target;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	public CommandProxyBuilder(Object target) {
		this(target, null);
	}

	public CommandProxyBuilder(Object target, CommandProxy proxy) {
		super(proxy);
		this.target = target;
	}

	/**
	 * A description of the command proxy produced by this builder.
	 */
	public CommandProxyBuilder description(String description, Object... params) {
		return this;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
