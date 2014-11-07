package org.spicefactory.lib.command.lifecycle;

import org.spicefactory.lib.command.CommandResult;
import org.spicefactory.lib.command.data.CommandData;

/**
 * Represents the life-cycle of a command.
 * <p>
 * This is a hook that can be used by frameworks to add functionality to the command execution.
 * <p>
 * A typical example is integration into an IOC container where the command is supposed to be managed by the container for the time it is getting
 * executed and where the creation of the command instance may also be managed by the container. But this in entirely generic hook that can be
 * used for any kind of additional functionality.
 * @author Sylvain Lecoy <sylvain.lecoy@gmail.com>
 */
public interface CommandLifecycle {

	/**
	 * Creates a new command instance of the specified type.
	 * <p>
	 * The data passed to this method may be used to perform constructor injection or similar tasks.
	 * @param type the type of command to create
	 * @param data the data passed to the command by the executor
	 * @return a new command instance.
	 */
	<T> T createInstance(Class<T> type, CommandData data);

	/**
	 * Life-cycle hook to be invoked immediately before the command gets executed.
	 * @param command the command to be executed
	 * @param data the data passed to the command by the executor.
	 */
	void beforeExecution(Object command, CommandData data);

	/**
	 * Life-cycle hook to be invoked after the command finished execution. This includes successful completion as well as cancellation and
	 * errors.
	 * @param command the command that finished executing
	 * @param result the result produced by the command.
	 */
	void afterCompletion(Object command, CommandResult result);
}
