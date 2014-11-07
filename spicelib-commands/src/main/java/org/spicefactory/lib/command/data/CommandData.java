package org.spicefactory.lib.command.data;

import java.util.List;

/**
 * Represents the data produced by commands, usually inside a group or flow.
 * @author Sylvain Lecoy <sylvain.lecoy@gmail.com>
 */
public interface CommandData {

	/**
	 * Returns the last matching result that was added to this instance.
	 * <p>
	 * When no matching result was added this method returns null.
	 * @return the last result added to this instance.
	 */
	Object getObject();

	/**
	 * Returns the result of the specified type if any command has produced a matching result.
	 * <p>
	 * In case of multiple matches the last matching result that was added to this instance is returned.
	 * <p>
	 * When no matching result was added this method returns null.
	 * @param type the type of the result to return
	 * @return the last result added to this instance with a matching type.
	 */
	Object getObject(Class<?> type);

	/**
	 * Returns all results.
	 * <p>
	 * When no matching result was added this method returns an empty Array.
	 * <p>
	 * When a flow or group contains nested flows or groups their result is represented by a separate <code>CommandData</code> instance.
	 * @return an Array holding all results that were added to this instance.
	 */
	List<Object> getObjects();

	/**
	 * Returns all results of the specified type.
	 * <p>
	 * When no matching result was added this method returns an empty Array.
	 * <p>
	 * When a flow or group contains nested flows or groups their result is represented by a separate <code>CommandData</code> instance.
	 * @param type the type of the results to return (if omitted all types are included)
	 * @return an Array holding all matching results that were added to this instance.
	 */
	List<Object> getObjects(Class<?> type);

}
