package org.spicefactory.lib.command.builder;

import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.command.proxy.CommandProxy;
import org.spicefactory.lib.command.proxy.DefaultCommandProxy;
import org.spicefactory.lib.event.EventListener;

/**
 * A builder DSL for creating CommandProxy instances, responsible for executing a single command.
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class CommandProxyBuilder extends AbstractCommandBuilder {

	private final Object target;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	public CommandProxyBuilder(Object target) {
		this(target, new DefaultCommandProxy());
	}

	public CommandProxyBuilder(Object target, DefaultCommandProxy proxy) {
		super(proxy);
		this.target = target;
	}

	/**
	 * Sets the timeout for the command execution.
	 * <p>
	 * When the specified amount of time is elapsed the proxy will abort with an error.
	 * </p>
	 * @param milliseconds the timeout for the command execution in milliseconds
	 * @return this builder instance for method chaining
	 */
	public CommandProxyBuilder timeout(long milliseconds) {
		setTimeout(milliseconds);
		return this;
	}

	/**
	 * Adds a value that can get passed to the command executed by the proxy this builder creates.
	 * @param value the value to pass to the command
	 * @return this builder instance for method chaining
	 */
	public CommandProxyBuilder data(Object value) {
		addData(value);
		return this;
	}

	/**
	 * Adds a callback to invoke when the command completes successfully.
	 * <p>
	 * The result produced by the command will get passed to the callback.
	 * </p>
	 * @param callback the callback to invoke when the command completes successfully
	 * @return this builder instance for method chaining
	 */
	public CommandProxyBuilder result(EventListener<CommandEvent> callback) {
		addResultCallback(callback);
		return this;
	}

	/**
	 * Adds a callback to invoke when the command raised an exception.
	 * <p>
	 * The cause of the exception will get passed to the callback.
	 * </p>
	 * @param callback the callback to invoke when the command produced an error
	 * @return this builder instance for method chaining
	 */
	public CommandProxyBuilder exception(EventListener<CommandEvent> callback) {
		addExceptionCallback(callback);
		return this;
	}

	/**
	 * Adds a callback to invoke when the command gets cancelled.
	 * <p>
	 * The callback should not expect any parameters.
	 * </p>
	 * @param callback the callback to invoke when the command gets cancelled
	 * @return this builder instance for method chaining
	 */
	public CommandProxyBuilder cancel(EventListener<CommandEvent> callback) {
		addCancelCallback(callback);
		return this;
	}

	@Override
	public CommandProxy build() {
		if (target instanceof Class<?>) {
			setType((Class<?>) target);
		} else {
			setTarget(asCommand(target));
		}
		return super.build();
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
