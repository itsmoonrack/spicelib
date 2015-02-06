package org.spicefactory.lib.command.adapter;

import org.spicefactory.lib.command.CommandExecutor;

/**
 * Represents an adapter for a command type that does not implement one of the command interfaces.
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public interface CommandAdapter extends CommandExecutor {

	/**
	 * The target executed by this adapter.
	 */
	Object getTarget();

}
