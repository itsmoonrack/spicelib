package org.spicefactory.lib.command.base;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.spicefactory.lib.command.AsyncCommand;
import org.spicefactory.lib.command.Command;
import org.spicefactory.lib.command.CommandExecutor;
import org.spicefactory.lib.command.CommandUtil;
import org.spicefactory.lib.command.SuspendableCommand;
import org.spicefactory.lib.command.data.CommandData;
import org.spicefactory.lib.command.data.DefaultCommandData;
import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.command.events.CommandResultEvent;
import org.spicefactory.lib.command.lifecycle.CommandLifecycle;
import org.spicefactory.lib.event.EventListener;

public abstract class AbstractCommandExecutor extends AbstractSuspendableCommand implements CommandExecutor {

	private DefaultCommandData data;

	private final List<Command> activeCommands = new LinkedList<Command>();

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
		// TODO Auto-generated method stub

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
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("rawtypes")
	private final Map<AsyncCommand, EventListener[]> listeners = new HashMap<AsyncCommand, EventListener[]>();

	@SuppressWarnings("rawtypes")
	private void addListeners(AsyncCommand command) {
		//		command.addEventListener(CommandResultEvent.COMPLETE, e -> commandCompleteHandler(e)); // Java 8
		//		command.addEventListener(CommandResultEvent.ERROR, e -> commandErrorHandler(e)); // Java 8
		//		command.addEventListener(CommandEvent.CANCEL, e -> commandCancelHandler(e)); // Java 8

		EventListener[] listenersGroup = new EventListener[3];
		command.addEventListener(CommandResultEvent.COMPLETE, listenersGroup[0] = new CommandCompleteHandler());
		command.addEventListener(CommandResultEvent.ERROR, listenersGroup[1] = new CommandErrorHandler());
		command.addEventListener(CommandEvent.CANCEL, listenersGroup[2] = new CommandCancelHandler());

		listeners.put(command, listenersGroup);
	}

	@SuppressWarnings("rawtypes")
	private void removeListeners(AsyncCommand command) {
		//		command.removeEventListener(CommandResultEvent.COMPLETE, e -> commandCompleteHandler(e)); // Java 8
		//		command.removeEventListener(CommandResultEvent.ERROR, e -> commandErrorHandler(e)); // Java 8
		//		command.removeEventListener(CommandEvent.CANCEL, e -> commandCancelHandler(e)); // Java 8

		EventListener[] listenersGroup = listeners.get(command);
		command.removeEventListener(CommandResultEvent.COMPLETE, listenersGroup[0]);
		command.removeEventListener(CommandResultEvent.ERROR, listenersGroup[1]);
		command.removeEventListener(CommandEvent.CANCEL, listenersGroup[2]);
	}

	private void commandCompleteHandler(CommandResultEvent e) {
		//
	}

	private class CommandCancelHandler implements EventListener<CommandResultEvent> {

		@Override
		public void process(CommandResultEvent event) {
			// TODO Auto-generated method stub

		}

	}

	private class CommandErrorHandler implements EventListener<CommandResultEvent> {

		@Override
		public void process(CommandResultEvent event) {
			// TODO Auto-generated method stub

		}

	}

	private class CommandCompleteHandler implements EventListener<CommandEvent> {

		@Override
		public void process(CommandEvent event) {
			// TODO Auto-generated method stub

		}

	}
}
