package org.spicefactory.lib.command.builder;

import org.spicefactory.lib.command.Command;
import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.command.events.CommandResultEvent;
import org.spicefactory.lib.command.proxy.CommandProxy;
import org.spicefactory.lib.command.proxy.DefaultCommandProxy;
import org.spicefactory.lib.event.EventListener;

/**
 * Abstract base class for all builder types.
 * <p>
 * The builder always produces a proxy responsible for executing the actual command.
 * </p>
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public abstract class AbstractCommandBuilder implements CommandBuilder {

	private final DefaultCommandProxy proxy;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	protected AbstractCommandBuilder() {
		this(new DefaultCommandProxy());
	}

	protected AbstractCommandBuilder(DefaultCommandProxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * Sets the target command to execute.
	 * @param target the target command to execute
	 */
	protected void setTarget(Command target) {
		proxy.setTarget(target);
	}

	/**
	 * Sets the type of the command to execute.
	 * <p>
	 * In this case the actual instance will be created by the proxy.
	 * </p>
	 * @param type the type of the command to execute
	 */
	protected void setType(Class<?> type) {
		proxy.setType(type);
	}

	/**
	 * Adds a value that can get passed to any command executed by the command proxy this builder creates.
	 * @param value the value to pass to the command proxy
	 */
	protected void addData(Object value) {
		proxy.addData(value);
	}

	/**
	 * Sets the timeout for this proxy.
	 * <p>
	 * When the specified amount of time is elapsed the command execution will abort with an error.
	 * </p>
	 * @param milliseconds the timeout for this proxy in milliseconds
	 */
	protected void setTimeout(long milliseconds) {
		proxy.setTimeout(milliseconds);
	}

	/**
	 * Adds a callback to invoke when the target command completes successfully.
	 * <p>
	 * The result produced by the command will get passed to the callback.
	 * @param callback the callback to invoke when the target command completes successfully
	 */
	protected void addResultCallback(EventListener<CommandEvent> callback) {
		proxy.addEventListener(CommandResultEvent.COMPLETE, callback);
	}

	/**
	 * Adds a callback to invoke when the target command raised an exception.
	 * <p>
	 * The cause of the exception will be passed to the callback.
	 * @param callback the callback to invoke when the target command raised an exception
	 */
	protected void addExceptionCallback(EventListener<CommandEvent> callback) {
		proxy.addEventListener(CommandResultEvent.EXCEPTION, callback);
	}

	/**
	 * Adds a callback to invoke when the target command gets cancelled.
	 * <p>
	 * The callback should not expect any parameters.
	 * @param callback the callback to invoke when the target command gets cancelled
	 */
	protected void addCancelCallback(EventListener<CommandEvent> callback) {
		proxy.addEventListener(CommandResultEvent.CANCEL, callback);
	}

	/**
	 * Turns the specified instance into a command that can be executed by the proxy created by this builder.
	 * <p>
	 * Legal parameters are any instances that implement either <code>Command</code> or <code>CommandBuilder</code>, a <code>Class</code>
	 * reference that specifies the type of the target command to create, or any other type in case an adapter is registered that knows how to
	 * turn the type into a command.
	 * </p>
	 * @param the instance to turn into a command
	 * @return the command created from the specified instance
	 */
	protected Command asCommand(Object command) {
		if (command instanceof Command) {
			return (Command) command;
		} else if (command instanceof CommandBuilder) {
			return ((CommandBuilder) command).build();
		} else if (command instanceof Class<?>) {
			return Commands.create((Class<?>) command).build();
		} else {
			return null; // TODO: CommandAdapters implementation.
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	public CommandProxy execute() {
		CommandProxy proxy = build();
		proxy.execute();
		return proxy;
	}

	@Override
	public CommandProxy build() {
		return proxy;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
