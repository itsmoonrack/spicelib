package org.spicefactory.lib.command.proxy;

import org.spicefactory.lib.command.Command;
import org.spicefactory.lib.command.base.AbstractCommandExecutor;
import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.command.lifecycle.CommandLifecycle;
import org.spicefactory.lib.event.EventListener;

public class DefaultCommandProxy extends AbstractCommandExecutor implements CommandProxy, EventListener<CommandEvent> {

	private Class<?> type;
	private Command target;
	private String proxyDescription;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * The target command that this proxy should execute.
	 * <p>
	 * The <code>type</code> and <code>target</code> properties are mutually exclusive.
	 * </p>
	 */
	public void setTarget(Command value) {
		this.target = value;
		this.type = null;
	}

	@Override
	public Command getTarget() {
		return target;
	}

	/**
	 * The type of command that this proxy should execute.
	 * <p>
	 * The <code>type</code> and <code>target</code> properties are mutually exclusive.
	 * </p>
	 */
	public void setType(Class<?> value) {
		this.type = value;
		this.target = null;
	}

	/**
	 * A description of the command executed by this proxy.
	 */
	public void setDescription(String value) {
		this.proxyDescription = value;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	protected CommandLifecycle createLifecycle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doExecute() {
		if (target == null && type == null) {
			throw new IllegalStateException("Either target or type property must be set.");
		}
		if (target == null) {
			//			try {
			//				Object target = lifecycle().createInstance(type, data);
			//				target = target instanceof Command ?  (Command) target : CommandAdapters.createAdapter(target);
			//			}
		}

		lifecycle().createInstance(type, data);

	}

	@Override
	public void process(CommandEvent event) {
		// TODO Auto-generated method stub

	}
}
