package org.spicefactory.lib.command.callback;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
// @FunctionalInterface
public interface ExceptionCallback<E extends Throwable> {

	void exception(E e);

}
