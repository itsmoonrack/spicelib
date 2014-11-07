package org.spicefactory.lib.command;

import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.event.annotation.Event;

/**
 * Represents a command that can get cancelled.
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
@Event(type = CommandEvent.class, id = CommandEvent.CANCEL)
public interface CancellableCommand extends AsyncCommand {

	/**
	 * Cancels the command.
	 */
	void cancel();

}
