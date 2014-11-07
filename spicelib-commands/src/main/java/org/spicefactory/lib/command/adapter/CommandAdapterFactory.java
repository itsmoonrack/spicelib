package org.spicefactory.lib.command.adapter;


/**
 * Represents a factory for command adapters.
 * @author Sylvain Lecoy <sylvain.lecoy@gmail.com>
 */
public interface CommandAdapterFactory {

	/**
	 * Creates a new adapter for the specified target command.
	 * @param instance the target command that usually does not implement one of the Command interfaces
	 * @return a new adapter for the specified target command or null if the specified instance cannot be handled by this factory
	 */
	CommandAdapter createAdapter(Object instance);

}
