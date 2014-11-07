package org.spicefactory.lib.command.base;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.spicefactory.lib.command.Command;
import org.spicefactory.lib.command.CommandExecutor;
import org.spicefactory.lib.command.CommandUtil;
import org.spicefactory.lib.command.SuspendableCommand;
import org.spicefactory.lib.command.data.CommandData;
import org.spicefactory.lib.command.data.DefaultCommandData;
import org.spicefactory.lib.command.lifecycle.CommandLifecycle;

public abstract class AbstractCommandExecutor extends AbstractSuspendableCommand implements CommandExecutor {

	private DefaultCommandData data;

	private final List<Command> activeCommands = new LinkedList<Command>();

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

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

	public boolean isCancellable() {
		for (Command c : activeCommands) {
			if (!CommandUtil.isCancellable(c)) {
				return false;
			}
		}
		return true;
	}

	public boolean isSuspendable() {
		for (Command c : activeCommands) {
			if (!CommandUtil.isSuspendable(c)) {
				return false;
			}
		}
		return true;
	}

	public void prepare(CommandLifecycle lifecycle, CommandData data) {
		// TODO Auto-generated method stub

	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
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
		// TODO Auto-generated method stub

	}
}
