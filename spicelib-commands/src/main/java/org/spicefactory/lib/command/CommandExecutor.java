package org.spicefactory.lib.command;

import org.spicefactory.lib.command.data.CommandData;
import org.spicefactory.lib.command.lifecycle.CommandLifecycle;

/**
 * Represents a command that executes one or more other commands.
 * <p>
 * This is the base interface for all command types that group, link or proxy other commands. Since these commands may implement any of the
 * optional <code>Command</code> sub-interface, this interface introduces properties that determine the capabilities of this executor.
 * </p>
 * @author Sylvain Lecoy <sylvain.lecoy@gmail.com>
 */
public interface CommandExecutor extends SuspendableCommand {

	/**
	 * Indicates whether this executor can be cancelled.
	 * <p>
	 * This property should be true when all currently active commands can be cancelled.
	 * </p>
	 */
	boolean isCancellable();

	/**
	 * Indicates whether this executor can be suspended.
	 * <p>
	 * This property should be true when all currently active commands can be suspended.
	 * </p>
	 */
	boolean isSuspendable();

	/**
	 * Method that may be called by frameworks before executing this command to hook into the life-cycle and data handling of this executor.
	 * @param lifecycle the life-cycle hooks this executor should use
	 * @param data data that can be passed to commands executed by this instance
	 */
	void prepare(CommandLifecycle lifecycle, CommandData data);

}
