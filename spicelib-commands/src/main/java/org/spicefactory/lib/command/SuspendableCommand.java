package org.spicefactory.lib.command;

import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.event.annotation.Event;
import org.spicefactory.lib.event.annotation.Events;

/**
 * Represents a command that can get cancelled and suspended.
 * @author Sylvain Lecoy <sylvain.lecoy@gmail.com>
 */
@Events({@Event(type = CommandEvent.class, id = CommandEvent.SUSPEND), @Event(type = CommandEvent.class, id = CommandEvent.RESUME)})
public interface SuspendableCommand extends CancellableCommand {

	/**
	 * Indicates whether this command is currently suspended.
	 */
	boolean isSuspended();

	/**
	 * Suspends the command.
	 * <p>
	 * Calling this method only has an effect if the command is currently executing.
	 */
	void suspend();

	/**
	 * Resumes the command.
	 * <p>
	 * Calling this method only has an effect if the command is currently suspended.
	 */
	void resume();

}
