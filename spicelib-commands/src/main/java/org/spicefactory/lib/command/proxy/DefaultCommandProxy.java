package org.spicefactory.lib.command.proxy;

import java.util.Timer;
import java.util.TimerTask;

import org.spicefactory.lib.command.Command;
import org.spicefactory.lib.command.adapter.CommandAdapterFactory;
import org.spicefactory.lib.command.base.AbstractCommandExecutor;
import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.command.events.CommandException;
import org.spicefactory.lib.command.events.CommandResultEvent;
import org.spicefactory.lib.command.events.CommandTimeoutException;
import org.spicefactory.lib.event.EventListener;

public class DefaultCommandProxy extends AbstractCommandExecutor implements CommandProxy, EventListener<CommandEvent> {

	private long delay;
	private Timer timer;
	private Class<?> type;
	private Command target;

	/** Optional factory dependency. */
	protected CommandAdapterFactory factory;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	public DefaultCommandProxy() {
		addEventListener(CommandResultEvent.COMPLETE, this);
		addEventListener(CommandResultEvent.EXCEPTION, this);
		addEventListener(CommandEvent.CANCEL, this);
		addEventListener(CommandEvent.SUSPEND, this);
		addEventListener(CommandEvent.RESUME, this);
	}

	/**
	 * The target command that this proxy should execute.
	 * <p>
	 * The <code>type</code> and <code>target</code> properties are mutually exclusive.
	 * </p>
	 */
	public void setTarget(Command value) {
		target = value;
		type = null;
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
		target = null;
	}

	@Override
	public Command getTarget() {
		return target;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	public void process(CommandEvent event) {
		switch (event.getID()) {
			case CommandEvent.RESUME:
				scheduleTimer();
				break;
			case CommandEvent.CANCEL:
			case CommandEvent.SUSPEND:
			case CommandResultEvent.COMPLETE:
			case CommandResultEvent.EXCEPTION:
				cancelTimer();
				break;
		}
	}

	@Override
	protected void doExecute() {
		if (target == null && type == null) {
			throw new IllegalStateException("Either target or type property must be set.");
		}
		if (target == null) {
			try {
				Object command = getLifecycle().createInstance(type, getData());
				target = command instanceof Command ? (Command) command : factory.createAdapter(command);
			}
			catch (Throwable cause) {
				exception(new CommandException(this, target, cause));
				return;
			}
		}
		executeCommand(target);
		scheduleTimer();
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
			logger.error("Internal error: timeout in command '{0}' although it is not active.", this);
		}
		timer = null;
	}

	private void cancelTimer() {
		timer.cancel();
		timer = null;
	}
}
