package org.spicefactory.lib.command.group;

import org.spicefactory.lib.command.Command;
import org.spicefactory.lib.command.CommandExecutor;

/**
 * Represents a group of commands.
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public interface CommandGroup extends CommandExecutor {

	/**
	 * Adds a command to this group.
	 * @param command to add to this group.
	 */
	void addCommand(Command command);

}
