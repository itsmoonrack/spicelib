package org.spicefactory.lib.command.group;

import java.util.Vector;

import org.spicefactory.lib.command.Command;
import org.spicefactory.lib.command.CommandResult;
import org.spicefactory.lib.command.base.AbstractCommandExecutor;

/**
 * A CommandGroup implementation that executes its child commands sequentially.
 * <p>
 * When the last child command has completed its operation this sequence will fire its <code>COMPLETE</code> event. If the sequence gets
 * cancelled or suspended the currently active child command will also be cancelled or suspended in turn.
 * </p>
 * <p>
 * If a child command throws an <code>ERROR</code> event and the <code>skipErrors</code> property of this sequence is set to false, then the
 * sequence will fire an <code>ERROR</code> event and will not execute its remaining child commands.
 * </p>
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class CommandSequence extends AbstractCommandExecutor implements CommandGroup {

	private int currentIndex; // This does not need synchronization/concurrent access.
	private final Vector<Command> commands = new Vector<Command>();

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new sequence.
	 * @param skipErrors if true an error in a command executed by this instance leads to commandComplete getting called, if false the executor
	 *            will stop with an error result
	 * @param skipCancelllations if true the cancellation of a command executed by this instance leads to commandComplete getting called, if
	 *            false the executor will stop with an error result
	 */
	public CommandSequence(boolean skipErrors, boolean skipCancellations) {
		super(skipErrors, skipCancellations);
	}

	@Override
	public void addCommand(Command command) {
		commands.add(command);
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	protected void doExecute() {
		currentIndex = 0;
		nextCommand();
	}

	@Override
	protected void commandComplete(CommandResult result) {
		currentIndex++;
		nextCommand();
	}

	private void nextCommand() {
		if (commands.size() == currentIndex) {
			logger.info("Completed all commands in {}.", this);
			complete();
		} else {
			Command command = commands.get(currentIndex);
			logger.info("Executing next command {} in sequence {}.", command, this);
			executeCommand(command);
		}
	}
}