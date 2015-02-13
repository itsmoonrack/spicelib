package org.spicefactory.lib.command.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.spicefactory.lib.command.base.AbstractSuspendableCommand;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class FullCommand extends AbstractSuspendableCommand {

	public Integer executions = 0;
	public Integer completions = 0;
	public Integer exceptions = 0;
	public Integer cancellations = 0;
	public Integer suspensions = 0;
	public Integer resumptions = 0;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	public void forceCompletion() {
		forceCompletion(null);
	}

	public void forceCompletion(Object result) {
		completions++;
		complete(result);
	}

	public void forceException() {
		forceException(null);
	}

	public void forceException(Throwable cause) {
		exceptions++;
		exception(cause);
	}

	public void assertStatus(boolean active, int executions) {
		assertStatus(active, executions, 0, 0, 0, 0, 0);
	}

	public void assertStatus(boolean active, int executions, int completions) {
		assertStatus(active, executions, completions, 0, 0, 0, 0);
	}

	public void assertStatus(boolean active, int executions, int completions, int exceptions) {
		assertStatus(active, executions, completions, exceptions, 0, 0, 0);
	}

	public void assertStatus(boolean active, int executions, int completions, int exceptions, int cancellations) {
		assertStatus(active, executions, completions, exceptions, cancellations, 0, 0);
	}

	public void assertStatus(boolean active, int executions, int completions, int exceptions, int cancellations, int suspensions) {
		assertStatus(active, executions, completions, exceptions, cancellations, suspensions, 0);
	}

	public void assertStatus(boolean active, int executions, int completions, int exceptions, int cancellations, int suspensions, int resumptions) {
		assertThat(this.isActive(), equalTo(active));
		assertThat(this.executions, equalTo(executions));
		assertThat(this.completions, equalTo(completions));
		assertThat(this.exceptions, equalTo(exceptions));
		assertThat(this.cancellations, equalTo(cancellations));
		assertThat(this.suspensions, equalTo(suspensions));
		assertThat(this.resumptions, equalTo(resumptions));
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	@Override
	protected void doCancel() {
		cancellations++;
	}

	@Override
	protected void doResume() {
		resumptions++;
	}

	@Override
	protected void doSuspend() {
		suspensions++;
	}

	@Override
	protected void doExecute() {
		executions++;
	}
}
