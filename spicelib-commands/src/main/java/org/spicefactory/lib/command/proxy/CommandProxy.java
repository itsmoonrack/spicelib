package org.spicefactory.lib.command.proxy;

import org.spicefactory.lib.command.Command;
import org.spicefactory.lib.command.CommandExecutor;

/**
 * Represents a proxy that executes a single command.
 * <p>
 * A proxy is usually used to wrap additional functionality around a command, like timeout handling for example.
 * </p>
 * @author Sylvain Lecoy <sylvain.lecoy@gmail.com>
 */
public interface CommandProxy extends CommandExecutor {

	/**
	 * The target command executed by this proxy.
	 */
	Command getTarget();

}
