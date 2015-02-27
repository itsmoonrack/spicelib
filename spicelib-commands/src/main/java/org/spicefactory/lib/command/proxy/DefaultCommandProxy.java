package org.spicefactory.lib.command.proxy;

import java.util.Timer;
import java.util.TimerTask;

import org.spicefactory.lib.command.Command;
import org.spicefactory.lib.command.CommandResult;
import org.spicefactory.lib.command.adapter.CommandAdapters;
import org.spicefactory.lib.command.base.AbstractCommandExecutor;
import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.command.events.CommandException;
import org.spicefactory.lib.command.events.CommandResultEvent;
import org.spicefactory.lib.command.events.CommandTimeoutException;
import org.spicefactory.lib.event.EventListener;

public class DefaultCommandProxy extends AbstractCommandExecutor implements CommandProxy {

	private long delay;
	private Timer timer;
	private Class<?> type;
	private Command target;
	private String description;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	public DefaultCommandProxy() {
		// Java 1.8:
		//addEventListener(CommandResultEvent.COMPLETE, scheduleTimer());
		addEventListener(CommandResultEvent.COMPLETE, new CommandInactive());
		addEventListener(CommandResultEvent.EXCEPTION, new CommandInactive());
		addEventListener(CommandEvent.CANCEL, new CommandInactive());
		addEventListener(CommandEvent.SUSPEND, new CommandInactive());
		addEventListener(CommandEvent.RESUME, new CommandActive());
	}

	/**
	 * The target command that this proxy should execute.
	 * <p>
	 * The <code>type</code> and <code>target</code> properties are mutually exclusive.
	 * </p>
	 */
	public void setTarget(Command value) {
		target = value;
	}

	/**
	 * The timeout in milliseconds. A value of 0 disables the timeout.
	 * @param milliseconds
	 */
	public void setTimeout(long milliseconds) {
		delay = milliseconds;
	}

	/**
	 * The type of command that this proxy should execute.
	 * <p>
	 * The <code>type</code> and <code>target</code> properties are mutually exclusive.
	 * </p>
	 */
	public void setType(Class<?> value) {
		type = value;
	}

	/**
	 * A description of the command executed by this proxy.
	 */
	public void setDescription(String value) {
		description = value;
	}

	@Override
	public Command getTarget() {
		return target;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	protected void doExecute() {
		if (target == null && type == null) {
			throw new IllegalStateException("Either target or type property must be set.");
		}
		if (target == null) {
			try {
				Object command = getLifecycle().createInstance(type, getData());
				target = command instanceof Command ? (Command) command : CommandAdapters.createAdapter(command);
			}
			catch (Throwable cause) {
				exception(new CommandException(this, target, cause));
				return;
			}
		}
		executeCommand(target);
		scheduleTimer();
	}

	@Override
	protected void commandComplete(CommandResult result) {
		complete(result.getValue());
	}

	private void scheduleTimer() {
		cancelTimer();
		if (delay > 0) {
			timer = new Timer();
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					onTimeout();
				}
			};
			timer.schedule(task, delay);
		}
	}

	private void onTimeout() {
		if (isActive()) {
			doCancel();
			exception(new CommandException(this, target, new CommandTimeoutException(delay)));
		} else {
			logger.error("Internal error: timeout in command '{0}' although it is not active.", target);
		}
		timer = null;
	}

	private void cancelTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	@Override
	public String toString() {
		return description != null ? description : target != null ? target.toString() : "LazyCommandProxy[" + type.toString() + "]";
	}
	// Java 1.6 legacy for Java 1.8.
	private class CommandInactive implements EventListener<CommandEvent> {

		@Override
		public void process(CommandEvent event) {
			cancelTimer();
		}

	}

	private class CommandActive implements EventListener<CommandEvent> {

		@Override
		public void process(CommandEvent event) {
			scheduleTimer();
		}

	}
}
