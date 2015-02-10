package org.spicefactory.lib.command.group;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.spicefactory.lib.command.Command;
import org.spicefactory.lib.command.CommandResult;
import org.spicefactory.lib.command.base.AbstractCommandExecutor;

/**
 * A CommandGroup implementation that executes its child commands in parallel.
 * <p>
 * If a group is started all the commands that were added to it will be started immediately.
 * <p>
 * If a command gets added to a running group, that command will be started immediately.
 * <p>
 * When all child commands have completed their operation this group will fire its <code>COMPLETE</code> event. If a group gets cancelled or
 * suspended all child commands that are still running will also be cancelled or suspended in turn.
 * </p>
 * <p>
 * If a child command throws an <code>EXCEPTION</code> event and the <code>skipExceptions</code> property of this group is set to false, then all
 * child commands that are still running will be cancelled and the group will fire an <code>EXCEPTION</code> event.
 * </p>
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class CommandParallel extends AbstractCommandExecutor implements CommandGroup {

	private final AtomicInteger completed = new AtomicInteger(0);
	private final Vector<Command> commands = new Vector<Command>();

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance.
	 * @param skipExceptions if true an error in a command executed by this instance leads to commandComplete getting called, if false the
	 *            executor will stop with an exception
	 * @param skipCancelllations if true the cancellation of a command executed by this instance leads to commandComplete getting called, if
	 *            false the executor will stop with an error result
	 */
	public CommandParallel(boolean skipExceptions, boolean skipCancellations) {
		super(skipExceptions, skipCancellations);
	}

	@Override
	public void addCommand(Command command) {
		commands.add(command);
		if (isActive()) {
			executeCommand(command);
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	protected void doExecute() {
		if (commands.size() == 0) {
			complete();
			return;
		}
		completed.set(0);
		for (Command command : commands) {
			executeCommand(command);
		}
	}

	@Override
	protected void commandComplete(CommandResult result) {
		if (completed.incrementAndGet() == commands.size()) {
			complete();
		}
	}

}
