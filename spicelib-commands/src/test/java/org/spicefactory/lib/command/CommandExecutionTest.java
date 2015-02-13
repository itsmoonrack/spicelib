package org.spicefactory.lib.command;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.spicefactory.lib.command.builder.CommandProxyBuilder;
import org.spicefactory.lib.command.builder.Commands;
import org.spicefactory.lib.command.events.CommandException;
import org.spicefactory.lib.command.events.CommandTimeoutException;
import org.spicefactory.lib.command.impl.AsynchronousCommand;
import org.spicefactory.lib.command.impl.CommandEventCounter;
import org.spicefactory.lib.command.impl.FullCommand;
import org.spicefactory.lib.command.impl.SynchronousCommand;
import org.spicefactory.lib.command.proxy.CommandProxy;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
@RunWith(MockitoJUnitRunner.class)
public class CommandExecutionTest {

	@Test
	public void testSynchronousCommand() {
		// Given
		SynchronousCommand sync = new SynchronousCommand();

		// When
		Commands.wrap(sync).execute();

		// Then
		assertThat(sync.executions, equalTo(1));
	}

	@Test
	public void testAsynchronousCommand() {
		// Given
		AsynchronousCommand async = new AsynchronousCommand();
		CommandEventCounter events = new CommandEventCounter(async);

		assertThat(async.executions, equalTo(0));
		assertThat(async.completions, equalTo(0));
		assertFalse(async.isActive());
		events.assertEvents(0);

		// When
		Commands.wrap(async).execute();

		// Then
		assertThat(async.executions, equalTo(1));
		assertThat(async.completions, equalTo(0));
		assertThat(async.exceptions, equalTo(0));
		assertTrue(async.isActive());
		events.assertEvents(0);

		// When
		async.forceCompletion();

		// Then
		assertThat(async.executions, equalTo(1));
		assertThat(async.completions, equalTo(1));
		assertThat(async.exceptions, equalTo(0));
		assertFalse(async.isActive());
		events.assertEvents(1);
	}

	@Test
	public void testCancellationOnTarget() {
		// Given
		FullCommand com = new FullCommand();
		CommandEventCounter targetEvents = new CommandEventCounter(com);

		com.assertStatus(false, 0, 0);
		targetEvents.assertEvents(0);

		// When
		AsyncCommand proxy = Commands.wrap(com).execute();
		CommandEventCounter proxyEvents = new CommandEventCounter(proxy);

		// Then
		com.assertStatus(true, 1, 0);
		targetEvents.assertEvents(0);
		proxyEvents.assertEvents(0);

		// When
		com.cancel();

		// Then
		com.assertStatus(false, 1, 0, 0, 1);
		targetEvents.assertEvents(0, 0, 1);
		proxyEvents.assertEvents(0, 0, 1);
	}

	@Test
	public void testCancellationOnProxy() {
		// Given
		FullCommand com = new FullCommand();
		CommandEventCounter targetEvents = new CommandEventCounter(com);

		com.assertStatus(false, 0, 0);
		targetEvents.assertEvents(0);

		// When
		CommandEventCounter proxyEvents = new CommandEventCounter();
		CancellableCommand proxy = execute(com, proxyEvents);

		// Then
		com.assertStatus(true, 1, 0);
		targetEvents.assertEvents(0);
		proxyEvents.assertEvents(0);
		proxyEvents.assertCallbacks(0);

		// When
		proxy.cancel();

		// Then
		com.assertStatus(false, 1, 0, 0, 1);
		targetEvents.assertEvents(0, 0, 1);
		proxyEvents.assertEvents(0, 0, 1);
		proxyEvents.assertCallbacks(0, 0, 1);
	}

	@Test
	public void testSuspension() {
		// Given
		FullCommand com = new FullCommand();
		CommandEventCounter targetEvents = new CommandEventCounter(com);

		com.assertStatus(false, 0, 0);
		targetEvents.assertEvents(0);

		// When
		CommandEventCounter proxyEvents = new CommandEventCounter();
		SuspendableCommand proxy = execute(com, proxyEvents);

		// Then
		com.assertStatus(true, 1, 0);
		targetEvents.assertEvents(0);
		proxyEvents.assertEvents(0);
		proxyEvents.assertCallbacks(0);

		// When
		proxy.suspend();

		// Then
		com.assertStatus(true, 1, 0, 0, 0, 1);
		targetEvents.assertEvents(0, 0, 0, 1);
		proxyEvents.assertEvents(0, 0, 0, 1);
		proxyEvents.assertCallbacks(0);

		// When
		proxy.resume();

		// Then
		com.assertStatus(true, 1, 0, 0, 0, 1, 1);
		targetEvents.assertEvents(0, 0, 0, 1, 1);
		proxyEvents.assertEvents(0, 0, 0, 1, 1);
		proxyEvents.assertCallbacks(0);

		// When
		com.forceCompletion();

		// Then
		com.assertStatus(false, 1, 1, 0, 0, 1, 1);
		targetEvents.assertEvents(1, 0, 0, 1, 1);
		proxyEvents.assertEvents(1, 0, 0, 1, 1);
		proxyEvents.assertCallbacks(1);
	}

	@Test
	public void testCreateCommand() {
		// Given
		SynchronousCommand.resetTotalExecutions();

		// When
		Commands.create(SynchronousCommand.class).execute();

		// Then
		assertThat(SynchronousCommand.totalExecutions, equalTo(1));
	}

	@Test
	public void testTimeout() throws InterruptedException {
		// Given
		CommandEventCounter proxyEvents = new CommandEventCounter();
		CommandProxyBuilder builder = Commands.create(AsynchronousCommand.class).timeout(100);
		addCallbacks(builder, proxyEvents);
		CommandProxy proxy = builder.execute();
		proxyEvents.setTarget(proxy);

		proxyEvents.assertEvents(0);
		proxyEvents.assertCallbacks(0);

		// When
		Thread.sleep(500);

		// Then
		proxyEvents.assertEvents(0, 1);
		proxyEvents.assertCallbacks(0, 1);
		assertThat(proxyEvents.getException(), is(instanceOf(CommandException.class)));
		CommandException exception = (CommandException) proxyEvents.getExceptionFromCallback();
		assertThat(exception.getCause(), is(instanceOf(CommandTimeoutException.class)));
		assertThat(exception.getExecutor(), sameInstance((CommandExecutor) proxy));
		assertThat(exception.getTarget(), is(instanceOf(AsynchronousCommand.class)));
	}

	@Test(expected = IllegalStateException.class)
	public void testIllegalSuspension() {
		// Given
		AsynchronousCommand async = new AsynchronousCommand();

		// When
		CommandProxy proxy = Commands.wrap(async).execute();

		// Then
		proxy.suspend();
	}

	@Test
	public void testSynchronousException() {
		// Given
		SynchronousCommand sync = new SynchronousCommand(true);
		CommandEventCounter proxyEvents = new CommandEventCounter();

		// When
		CommandProxy proxy = execute(sync, proxyEvents);

		// Then
		proxyEvents.assertEvents(0); // due to timing we don't get the event for a synchronous command here
		proxyEvents.assertCallbacks(0, 1);
		assertThat(proxyEvents.getExceptionFromCallback(), is(instanceOf(CommandException.class)));
		CommandException exception = (CommandException) proxyEvents.getExceptionFromCallback();
		assertThat(exception.getCause(), is(instanceOf(RuntimeException.class)));
		assertThat(exception.getExecutor(), sameInstance((CommandExecutor) proxy));
		assertThat(exception.getTarget(), sameInstance((Command) sync));
	}

	@Test
	public void testAsynchronousException() {
		AsynchronousCommand async = new AsynchronousCommand();
		CommandEventCounter proxyEvents = new CommandEventCounter();
		CommandProxy proxy = execute(async, proxyEvents);

		proxyEvents.assertEvents(0);
		proxyEvents.assertCallbacks(0);

		async.forceException(new Exception("Expected"));

		proxyEvents.assertEvents(0, 1);
		proxyEvents.assertCallbacks(0, 1);

		assertThat(proxyEvents.getException(), is(instanceOf(CommandException.class)));
		CommandException exception = (CommandException) proxyEvents.getExceptionFromCallback();
		assertThat(exception.getCause(), is(instanceOf(Exception.class)));
		assertThat(exception.getExecutor(), sameInstance((CommandExecutor) proxy));
		assertThat(exception.getTarget(), sameInstance((Command) async));
	}

	private CommandProxy execute(Command command, CommandEventCounter counter) {
		CommandProxy proxy = Commands.wrap(command) //
				.result(counter.resultCallback) //
				.cancel(counter.cancelCallback) //
				.exception(counter.exceptionCallback) //
				.execute();
		counter.setTarget(proxy);
		return proxy;
	}

	private void addCallbacks(CommandProxyBuilder builder, CommandEventCounter counter) {
		builder //
		.result(counter.resultCallback) //
				.cancel(counter.cancelCallback) //
				.exception(counter.exceptionCallback);
	}
}
