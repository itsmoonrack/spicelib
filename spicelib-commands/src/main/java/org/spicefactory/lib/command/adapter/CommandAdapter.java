package org.spicefactory.lib.command.adapter;

/**
 * Represents an adapter for a command type that does not implement one of the command interfaces.
 * @author Sylvain Lecoy <sylvain.lecoy@gmail.com>
 */
public interface CommandAdapter {

	/**
	 * The target executed by this adapter.
	 */
	Object getTarget();

}
