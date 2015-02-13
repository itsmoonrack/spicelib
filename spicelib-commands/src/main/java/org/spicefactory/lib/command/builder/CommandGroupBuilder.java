package org.spicefactory.lib.command.builder;

import java.util.ArrayList;
import java.util.List;

import org.spicefactory.lib.command.callback.CancelCallback;
import org.spicefactory.lib.command.callback.ExceptionCallback;
import org.spicefactory.lib.command.callback.ResultCallback;
import org.spicefactory.lib.command.data.CommandData;
import org.spicefactory.lib.command.group.CommandGroup;
import org.spicefactory.lib.command.group.CommandParallel;
import org.spicefactory.lib.command.group.CommandSequence;
import org.spicefactory.lib.command.proxy.CommandProxy;

/**
 * A builder DSL for creating CommandGroup instances.
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class CommandGroupBuilder extends AbstractCommandBuilder {

	private boolean skipExceptions;
	private boolean skipCancellations;

	private final boolean sequence;
	private final List<Object> commands;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	CommandGroupBuilder(boolean sequence) {
		this.sequence = sequence;
		this.commands = new ArrayList<Object>();
	}

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Adds a new command instance to this group.
	 * @param command the command to add to this group
	 * @return this builder instance for method chaining
	 */
	public CommandGroupBuilder add(Object command) {
		commands.add(command);
		return this;
	}

	/**
	 * Adds a new command type to this group.
	 * @param command the command type to add to this group
	 * @return this builder instance for method chaining
	 */
	public CommandGroupBuilder create(Class<?> command) {
		add(command);
		return this;
	}

	/**
	 * Sets the timeout for the group.
	 * <p>
	 * When the specified amount of time is elapsed the group execution will abort with an error.
	 * </p>
	 * @param milliseconds the timeout for this group in milliseconds
	 * @return this builder instance for method chaining
	 */
	public CommandGroupBuilder timeout(long milliseconds) {
		setTimeout(milliseconds);
		return this;
	}

	/**
	 * Adds a value that can get passed to any command executed by the group this builder creates.
	 * @param value the value to pass to the command group
	 * @return this builder instance for method chaining
	 */
	public CommandGroupBuilder data(Object value) {
		addData(value);
		return this;
	}

	/**
	 * Adds a callback to invoke when the command group completes successfully.
	 * <p>
	 * The result produced by the last command in the group will get passed to the callback.
	 * <p>
	 * It is not recommended to use this callback in case of parallel execution as the type of result passed to the callback might be different
	 * for each execution.
	 * @param callback the callback to invoke when the command group completes successfully
	 * @return this builder instance for method chaining
	 */
	@SuppressWarnings("unchecked")
	// Java 1.8 forward-compatibility.
	public <T> AbstractCommandBuilder lastResult(final ResultCallback<T> callback) {
		ResultCallback<CommandData> function = new ResultCallback<CommandData>() {
			@Override
			public void result(CommandData data) {
				callback.result((T) data.getObject());
			}
		};
		addResultCallback(function);
		return this;
	}

	/**
	 * Adds a callback to invoke when the command group completes successfully.
	 * <p>
	 * An instance of <code>CommandResult</code> will get passed to the callback holding all results produced by the commands in the group.
	 * @param callback the callback to invoke when the command group completes successfully
	 * @return this builder instance for method chaining
	 */
	public <T> CommandGroupBuilder allResults(final ResultCallback<T> callback) {
		addResultCallback(callback);
		return this;
	}

	/**
	 * Adds a callback to invoke when the command group produced an exception.
	 * <p>
	 * The cause of the error will get passed to the callback.
	 * </p>
	 * @param callback the callback to invoke when the command group produced an error
	 * @return this builder instance for method chaining
	 */
	public CommandGroupBuilder exception(ExceptionCallback<? super Throwable> callback) {
		addExceptionCallback(callback);
		return this;
	}

	/**
	 * Adds a callback to invoke when the command group gets cancelled.
	 * <p>
	 * The callback should not expect any parameters.
	 * </p>
	 * @param callback the callback to invoke when the command group gets cancelled
	 * @return this builder instance for method chaining
	 */
	public CommandGroupBuilder cancel(CancelCallback callback) {
		addCancelCallback(callback);
		return this;
	}

	/**
	 * Instructs the group to ignore exceptions produced by any of its commands and treat them the same way as successful completion. Without
	 * invoking this method the group will abort with an exception when any one command it executes produces an exception.
	 */
	public CommandGroupBuilder skipExceptions() {
		skipExceptions = true;
		return this;
	}

	/**
	 * Instructs the group to ignore cancellations of any of its commands and treat them the same way as successful completion. Without invoking
	 * this method the entire group will get cancelled when any one command it executes gats cancelled.
	 */
	public CommandGroupBuilder skipCancellations() {
		skipCancellations = true;
		return this;
	}

	@Override
	public CommandProxy build() {
		CommandGroup group =
				sequence ? new CommandSequence(skipExceptions, skipCancellations) : new CommandParallel(skipExceptions, skipCancellations);
		for (Object command : commands) {
			group.addCommand(asCommand(command));
		}
		setTarget(group);
		return super.build();
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
