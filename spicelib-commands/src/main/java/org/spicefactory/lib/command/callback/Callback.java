package org.spicefactory.lib.command.callback;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public interface Callback<T> extends ResultCallback<T>, ExceptionCallback<Throwable>, CancelCallback {

}
