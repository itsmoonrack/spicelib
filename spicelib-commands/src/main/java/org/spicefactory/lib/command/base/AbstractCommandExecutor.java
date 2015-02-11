package org.spicefactory.lib.command.base;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.spicefactory.lib.command.AsyncCommand;
import org.spicefactory.lib.command.CancellableCommand;
import org.spicefactory.lib.command.Command;
import org.spicefactory.lib.command.CommandExecutor;
import org.spicefactory.lib.command.CommandResult;
import org.spicefactory.lib.command.CommandUtil;
import org.spicefactory.lib.command.SuspendableCommand;
import org.spicefactory.lib.command.data.CommandData;
import org.spicefactory.lib.command.data.DefaultCommandData;
import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.command.events.CommandException;
import org.spicefactory.lib.command.events.CommandResultEvent;
import org.spicefactory.lib.command.lifecycle.CommandLifecycle;
import org.spicefactory.lib.command.lifecycle.DefaultCommandLifecycle;
import org.spicefactory.lib.event.EventListener;

/**
 * Abstract base class for all executor implementations.
 * <p>
 * It knows how to execute other commands and deal with their events. Subclasses are expected to call the protected <code>executeCommand</code>
 * method to start a command and override the protected template method <code>commandComplete</code> for dealing with the result.
 * </p>
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public abstract class AbstractCommandExecutor extends AbstractSuspendableCommand implements CommandExecutor {

	/** The life-cycle hook to use for the commands executed by this instance. */
	private CommandLifecycle lifecycle;

	private DefaultCommandData data;
	private final List<Object> values = new LinkedList<Object>();

	/** The active commands list. */
	private final List<Command> activeCommands = new LinkedList<Command>();

	private final boolean processExceptions;
	private final boolean processCancellations;

	/**
	 * Creates a new instance.
	 */
	protected AbstractCommandExecutor() {
		this(false, false);
	}

	/**
	 * Creates a new instance.
	 * @param description a description of this command
	 * @param processExceptions if true an error in a command executed by this instance leads to commandComplete getting called, if false the
	 *            executor will stop with an exception
	 * @param processCancellations if true the cancellation of a command executed by this instance leads to commandComplete getting called, if
	 *            false the executor will stop with an error result
	 */
	protected AbstractCommandExecutor(boolean processExceptions, boolean processCancellations) {
		this.processExceptions = processExceptions;
		this.processCancellations = processCancellations;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Adds a value to this executor that can get passed to any command executed by this instance.
	 * @param value the value to add to this executor
	 */
	public void addData(Object value) {
		if (data != null) {
			data.addValue(value);
		} else {
			values.add(value);
		}
	}

	@Override
	public void suspend() {
		if (!isSuspendable()) {
			throw new IllegalStateException(MessageFormat.format("Command '{0}' cannot be suspended.", this));
		}
		super.suspend();
	}

	@Override
	public void cancel() {
		if (!isCancellable()) {
			throw new IllegalStateException(MessageFormat.format("Command '{0}' cannot be cancelled.", this));
		}
		super.cancel();
	}

	@Override
	public boolean isCancellable() {
		for (Command c : activeCommands) {
			if (!CommandUtil.isCancellable(c)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isSuspendable() {
		for (Command c : activeCommands) {
			if (!CommandUtil.isSuspendable(c)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void prepare(CommandLifecycle lifecycle, CommandData data) {
		this.lifecycle = lifecycle;
		this.data = new DefaultCommandData(data);
		addValues();
	}

	/**
	 * Creates a new instance of the life-cycle hook.
	 * <p>
	 * Subclasses may override this method to provide specialized implementations. This method will only get invoked when the first command
	 * executed by this instance gets started without the <code>prepare</code> method being invoked up-front. The <code>prepare</code> method
	 * allows to pass down <code>CommandLifecycle</code> instances from the environment (like parent executors), in which case this instance
	 * should not create its own life-cycle.
	 * </p>
	 * @return a new life-cycle instance to use when executing commands
	 */
	protected CommandLifecycle createLifecycle() {
		return new DefaultCommandLifecycle();
	}

	/**
	 * The data associated with this executor.
	 * <p>
	 * Contains any results from previously executed commands or data specified upfront.
	 * </p>
	 * @return
	 */
	protected CommandData getData() {
		if (data == null) {
			CommandData newData = createData();
			data = (newData instanceof DefaultCommandData) ? (DefaultCommandData) newData : new DefaultCommandData(data);
			addValues();
		}

		return data;
	}

	/**
	 * The life-cycle hook to use for the commands executed by this instance.
	 */
	protected CommandLifecycle getLifecycle() {
		if (lifecycle == null) {
			lifecycle = createLifecycle();
		}
		return lifecycle;
	}

	/**
	 * Creates a new instance holding the data commands executed by this instance will produce.
	 * <p>
	 * Subclasses may override this method to provide specialized implementations.
	 * <p>
	 * This method will only get invoked when the first command executed by this instance gets started without the <code>prepare</code> method
	 * being invoked up-front. The <code>prepare</code> method allows to pass down <code>CommandData</code> instances from the environment (like
	 * parent executors), in which case this instance should not use its own implementations.
	 * @return a new instance to use for holding the data commands executed by this instance will produce
	 */
	protected CommandData createData() {
		return new DefaultCommandData();
	}

	private void addValues() {
		for (Object value : values) {
			data.addValue(value);
		}
		values.clear();
	}

	/////////////////////////////////////////////////////////////////////////////
	// Protected-overrides.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	protected void complete() {
		complete(data);
	}

	@Override
	protected void doSuspend() {
		for (Command c : activeCommands) {
			if (!((SuspendableCommand) c).isSuspended())
				((SuspendableCommand) c).suspend();
		}
	}

	@Override
	protected void doResume() {
		for (Command c : activeCommands) {
			if (((SuspendableCommand) c).isSuspended())
				((SuspendableCommand) c).resume();
		}
	}

	@Override
	protected void doCancel() {
		for (Command c : activeCommands) {
			removeListeners((AsyncCommand) c);
			if (CommandUtil.isCancellable(c)) {
				((CancellableCommand) c).cancel();
			}
		}
		activeCommands.clear();
	}

	/**
	 * Executes the specified command.
	 * <p>
	 * Upon completion the <code>commandComplete</code> method will get invoked which may be overridden by subclasses to deal with the result or
	 * decide on the next command to execute.
	 * </p>
	 * @param com the command to execute
	 */
	protected void executeCommand(Command command) {
		if (activeCommands.contains(command))
			return;

		activeCommands.add(command);

		if (command instanceof AsyncCommand) {
			addListeners((AsyncCommand) command);
			if (((AsyncCommand) command).isActive())
				return;
		}

		if (command instanceof CommandExecutor) {
			((CommandExecutor) command).prepare(lifecycle, data);
		}

		try {
			lifecycle.beforeExecution(command, data);
			logger.debug("Executing command '{}'.", command);
			command.execute();
		}
		catch (Exception e) {
			activeCommands.remove(command);
			commandException(command, e);
			return;
		}

		if (!(command instanceof AsyncCommand)) {
			activeCommands.remove(command);
			CommandResult result = DefaultCommandResult.forCompletion(command, null);
			lifecycle.afterCompletion(command, result);
			commandComplete(result);
		}
	}

	/**
	 * Invoked when a child command has completed its operation successfully.
	 * <p>
	 * It may also get invoked when a child command has been cancelled (in case <code>processCancellations</code> is set to true) and commands
	 * that failed (in case the <code>processErrors</code> is set to true).
	 * </p>
	 * @param result the result of the command
	 */
	protected void commandComplete(CommandResult result) {
		// Default implementation does nothing.
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("rawtypes")
	private final Map<AsyncCommand, EventListener[]> listeners = new HashMap<AsyncCommand, EventListener[]>();

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void addListeners(AsyncCommand command) {
		//		command.addEventListener(CommandResultEvent.COMPLETE, e -> commandCompleteHandler(e)); // Java 8
		//		command.addEventListener(CommandResultEvent.ERROR, e -> commandErrorHandler(e)); // Java 8
		//		command.addEventListener(CommandEvent.CANCEL, e -> commandCancelHandler(e)); // Java 8

		EventListener[] listenersGroup = new EventListener[3];
		command.addEventListener(CommandResultEvent.COMPLETE, listenersGroup[0] = new CommandCompleteHandler());
		command.addEventListener(CommandResultEvent.EXCEPTION, listenersGroup[1] = new CommandExceptionHandler());
		command.addEventListener(CommandEvent.CANCEL, listenersGroup[2] = new CommandCancelledHandler());

		listeners.put(command, listenersGroup);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void removeListeners(AsyncCommand command) {
		//		command.removeEventListener(CommandResultEvent.COMPLETE, e -> commandCompleteHandler(e)); // Java 8
		//		command.removeEventListener(CommandResultEvent.ERROR, e -> commandErrorHandler(e)); // Java 8
		//		command.removeEventListener(CommandEvent.CANCEL, e -> commandCancelHandler(e)); // Java 8

		EventListener[] listenersGroup = listeners.get(command);
		command.removeEventListener(CommandResultEvent.COMPLETE, listenersGroup[0]);
		command.removeEventListener(CommandResultEvent.EXCEPTION, listenersGroup[1]);
		command.removeEventListener(CommandEvent.CANCEL, listenersGroup[2]);
	}

	private void removeActiveCommand(AsyncCommand command, CommandResult result) {
		if (isSuspended()) {
			throw new IllegalStateException(MessageFormat.format("Child command {0} completed while executor was suspended.", command));
		}
		removeListeners(command);
		activeCommands.remove(command);
		lifecycle.afterCompletion(command, result);
	}

	private void commandCompleteHandler(CommandResultEvent event) {
		AsyncCommand command = (AsyncCommand) event.getSource();
		removeActiveCommand(command, event);
		data.addValue(event.getValue());
		commandComplete(event);
	}

	private void commandExceptionHandler(CommandResultEvent event) {
		AsyncCommand command = (AsyncCommand) event.getSource();
		removeActiveCommand(command, event);
		commandException(command, (Throwable) event.getValue());
	}

	private void commandException(Command command, Throwable cause) {
		if (processExceptions) {
			commandComplete(DefaultCommandResult.forException(command, cause));
		} else {
			doCancel();
			exception(new CommandException(this, command, cause));
		}
	}

	private void commandCancelledHandler(CommandEvent event) {
		AsyncCommand command = (AsyncCommand) event.getSource();
		CommandResult result = DefaultCommandResult.forCancellation(command);
		removeActiveCommand(command, result);
		if (processCancellations) {
			commandComplete(result);
		} else {
			cancel();
		}
	}

	// Java 1.6 legacy for Java 1.8.
	private class CommandCompleteHandler implements EventListener<CommandResultEvent> {

		@Override
		public void process(CommandResultEvent event) {
			commandCompleteHandler(event);
		}

	}

	private class CommandExceptionHandler implements EventListener<CommandResultEvent> {

		@Override
		public void process(CommandResultEvent event) {
			commandExceptionHandler(event);
		}

	}

	private class CommandCancelledHandler implements EventListener<CommandEvent> {

		@Override
		public void process(CommandEvent event) {
			commandCancelledHandler(event);
		}

	}
}
