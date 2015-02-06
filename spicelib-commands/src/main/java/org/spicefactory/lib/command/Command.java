package org.spicefactory.lib.command;

/**
 * Represents a single command. The base interface for all commands.
 * <p>
 * If only this interface is implemented by a command, it is treated as a synchronous command. For additional features like asynchronous
 * execution, cancellation or suspension, several sub-interfaces are available.
 * <p>
 * This interface is used by all internal command executors and builders. But application classes do not have to implement this interface when
 * they use the Light Command functionality where execution is based on naming conventions instead.
 * </p>
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public interface Command {

	/**
	 * Executes the command.
	 */
	void execute();

}
