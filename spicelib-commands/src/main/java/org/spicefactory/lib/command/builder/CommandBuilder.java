package org.spicefactory.lib.command.builder;

import org.spicefactory.lib.command.proxy.CommandProxy;

/**
 * Represents a builder that produces command proxies.
 * <p>
 * Most classes of the builder API implement this interfaces.
 * @author Sylvain Lecoy <sylvain.lecoy@gmail.com>
 */
public interface CommandBuilder {

	/**
	 * Builds and executes the target command.
	 * <p>
	 * A shortcut for calling <code>build().execute()</code>.
	 * <p>
	 * In case of asynchronous commands the returned proxy will still be active. In case of synchronous commands it will already be completed, so
	 * that adding event listeners won't have any effect.
	 * @return the command that was built and executed by this builder
	 */
	CommandProxy execute();

	/**
	 * Builds the target command, applying all configurations specified through this builder instance.
	 * @return the command proxy will all configuration of this builder applied
	 */
	CommandProxy build();

}
