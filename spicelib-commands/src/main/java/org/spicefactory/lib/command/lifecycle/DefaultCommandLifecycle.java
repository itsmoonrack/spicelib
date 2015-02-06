package org.spicefactory.lib.command.lifecycle;

import org.spicefactory.lib.command.CommandResult;
import org.spicefactory.lib.command.data.CommandData;

public class DefaultCommandLifecycle implements CommandLifecycle {

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	public <T> T createInstance(Class<T> type, CommandData data) {
		try {
			// Do not support constructor with parameters.
			return type.newInstance();
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public void beforeExecution(Object command, CommandData data) {
		/* Default implementation does nothing. */
	}

	@Override
	public void afterCompletion(Object command, CommandResult result) {
		/* Default implementation does nothing. */
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
