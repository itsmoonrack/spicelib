package org.spicefactory.lib.command.callback;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
// @FunctionalInterface
public interface ResultCallback<T> {

	void result(T result);

}
