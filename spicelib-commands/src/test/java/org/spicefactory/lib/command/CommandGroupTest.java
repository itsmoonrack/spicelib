package org.spicefactory.lib.command;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.spicefactory.lib.command.builder.CommandGroupBuilder;
import org.spicefactory.lib.command.builder.Commands;
import org.spicefactory.lib.command.events.CommandException;
import org.spicefactory.lib.command.events.CommandTimeoutException;
import org.spicefactory.lib.command.group.CommandGroup;
import org.spicefactory.lib.command.impl.AsynchronousCommand;
import org.spicefactory.lib.command.impl.CommandEventCounter;
import org.spicefactory.lib.command.impl.FullCommand;
import org.spicefactory.lib.command.impl.SynchronousCommand;
import org.spicefactory.lib.command.proxy.CommandProxy;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class CommandGroupTest {

	@Test
	public void testEmptySequentialComplete() {
		assertCompletion(Commands.asSequence());
	}

	@Test
	public void testEmptyConcurrentComplete() {
		assertCompletion(Commands.inParallel());
	}

	@Test
	public void testSyncSequentialComplete() {
		assertCompletion(Commands.asSequence().add(new SynchronousCommand()).add(new SynchronousCommand()));
	}

	@Test
	public void testSyncConcurrentComplete() {
		assertCompletion(Commands.inParallel().add(new SynchronousCommand()).add(new SynchronousCommand()));
	}

	private void assertCompletion(CommandGroupBuilder builder) {
		// Given
		CommandEventCounter events = new CommandEventCounter();
		addCallbacks(builder, events);
		CommandProxy proxy = builder.build();
		events.setTarget(proxy);

		events.assertEvents(0);
		events.assertCallbacks(0);

		// When
		proxy.execute();

		// Then
		events.assertEvents(1);
		events.assertCallbacks(1);
	}

	@Test
	public void testSequentialComplete() {
		AsynchronousCommand com1 = new AsynchronousCommand();
		AsynchronousCommand com2 = new AsynchronousCommand();
		assertAsyncCompletion(Commands.asSequence().add(com1).add(com2), com1, com2, false);
	}

	@Test
	public void testConcurrentComplete() {
		AsynchronousCommand com1 = new AsynchronousCommand();
		AsynchronousCommand com2 = new AsynchronousCommand();
		assertAsyncCompletion(Commands.inParallel().add(com1).add(com2), com1, com2, true);
	}

	private void assertAsyncCompletion(CommandGroupBuilder builder, AsynchronousCommand com1, AsynchronousCommand com2, boolean parallel) {
		// Given
		CommandEventCounter events = new CommandEventCounter();
		addCallbacks(builder, events);
		CommandProxy proxy = builder.build();
		events.setTarget(proxy);

		assertActive(com1, false);
		assertActive(com2, false);

		// When
		proxy.execute();

		// Then
		assertActive(com1, true);
		assertActive(com2, parallel);
		events.assertEvents(0);
		events.assertCallbacks(0);

		// When
		com1.forceCompletion();

		// Then
		assertActive(com1, false);
		assertActive(com2, true);
		events.assertEvents(0);
		events.assertCallbacks(0);

		// When
		com2.forceCompletion();

		// Then
		assertActive(com1, false);
		assertActive(com2, false);
		events.assertEvents(1);
		events.assertCallbacks(1);
	}

	private void assertActive(AsyncCommand com, boolean active) {
		assertThat(com.isActive(), equalTo(active));
	}

	private void assertSuspended(SuspendableCommand com, boolean suspended) {
		assertThat(com.isSuspended(), equalTo(suspended));
	}

	@Test
	public void testSequentialCancellation() {
		FullCommand com1 = new FullCommand();
		FullCommand com2 = new FullCommand();
		assertCancellation(Commands.asSequence().add(com1).add(com2), com1, com2, false);
	}

	@Test
	public void testParallelCancellation() {
		FullCommand com1 = new FullCommand();
		FullCommand com2 = new FullCommand();
		assertCancellation(Commands.inParallel().add(com1).add(com2), com1, com2, true);
	}

	private void assertCancellation(CommandGroupBuilder builder, FullCommand com1, FullCommand com2, boolean parallel) {
		// Given
		CommandEventCounter events = new CommandEventCounter();
		addCallbacks(builder, events);
		CommandProxy proxy = builder.build();
		events.setTarget(proxy);

		assertActive(com1, false);
		assertActive(com2, false);

		// When
		proxy.execute();

		// Then
		assertActive(com1, true);
		assertActive(com2, parallel);
		events.assertEvents(0);
		events.assertCallbacks(0);

		// When
		proxy.cancel();

		// Then
		assertActive(com1, false);
		assertActive(com2, false);
		events.assertEvents(0, 0, 1);
		events.assertCallbacks(0, 0, 1);
	}

	@Test
	public void testSequentialException() {
		FullCommand com1 = new FullCommand();
		FullCommand com2 = new FullCommand();
		assertException(Commands.asSequence().add(com1).add(com2), com1, com2, false);
	}

	@Test
	public void testParallelException() {
		FullCommand com1 = new FullCommand();
		FullCommand com2 = new FullCommand();
		assertException(Commands.inParallel().add(com1).add(com2), com1, com2, true);
	}

	private void assertException(CommandGroupBuilder builder, FullCommand com1, FullCommand com2, boolean parallel) {
		// Given
		CommandEventCounter events = new CommandEventCounter();
		addCallbacks(builder, events);
		CommandProxy proxy = builder.build();
		events.setTarget(proxy);

		assertActive(com1, false);
		assertActive(com2, false);

		// When
		proxy.execute();

		// Then
		assertActive(com1, true);
		assertActive(com2, parallel);
		events.assertEvents(0);
		events.assertCallbacks(0);

		// When
		com1.forceException();

		// Then
		assertActive(com1, false);
		assertActive(com2, false);
		events.assertEvents(0, 1);
		events.assertCallbacks(0, 1);
	}

	@Test
	public void testSequentialSkippedException() {
		FullCommand com1 = new FullCommand();
		FullCommand com2 = new FullCommand();
		assertSkippedException(Commands.asSequence().add(com1).add(com2).skipExceptions(), com1, com2, false);
	}

	@Test
	public void testParallelSkippedException() {
		FullCommand com1 = new FullCommand();
		FullCommand com2 = new FullCommand();
		assertSkippedException(Commands.inParallel().add(com1).add(com2).skipExceptions(), com1, com2, true);
	}

	private void assertSkippedException(CommandGroupBuilder builder, FullCommand com1, FullCommand com2, boolean parallel) {
		// Given
		CommandEventCounter events = new CommandEventCounter();
		addCallbacks(builder, events);
		CommandProxy proxy = builder.build();
		events.setTarget(proxy);

		assertActive(com1, false);
		assertActive(com2, false);

		// When
		proxy.execute();

		// Then
		assertActive(com1, true);
		assertActive(com2, parallel);
		events.assertEvents(0);
		events.assertCallbacks(0);

		// When
		com1.forceException();

		// Then
		assertActive(com1, false);
		assertActive(com2, true);
		events.assertEvents(0);
		events.assertCallbacks(0);

		// When
		com2.forceCompletion();

		// Then
		assertActive(com1, false);
		assertActive(com2, false);
		events.assertEvents(1);
		events.assertCallbacks(1);
	}

	@Test
	public void testSequentialSuspension() {
		FullCommand com1 = new FullCommand();
		FullCommand com2 = new FullCommand();
		assertSuspension(Commands.asSequence().add(com1).add(com2), com1, com2, false);
	}

	@Test
	public void testParallelSuspension() {
		FullCommand com1 = new FullCommand();
		FullCommand com2 = new FullCommand();
		assertSuspension(Commands.inParallel().add(com1).add(com2), com1, com2, true);
	}

	private void assertSuspension(CommandGroupBuilder builder, FullCommand com1, FullCommand com2, boolean parallel) {
		// Given
		CommandEventCounter events = new CommandEventCounter();
		addCallbacks(builder, events);
		CommandProxy proxy = builder.build();
		events.setTarget(proxy);

		assertActive(com1, false);
		assertActive(com2, false);

		// When
		proxy.execute();

		// Then
		assertActive(com1, true);
		assertActive(com2, parallel);
		assertSuspended(com1, false);
		assertSuspended(com2, false);
		events.assertEvents(0);
		events.assertCallbacks(0);

		// When
		proxy.suspend();

		// Then
		assertActive(com1, true);
		assertActive(com2, parallel);
		assertSuspended(com1, true);
		assertSuspended(com2, parallel);
		events.assertEvents(0, 0, 0, 1);
		events.assertCallbacks(0);

		// When
		proxy.resume();

		// Then
		assertActive(com1, true);
		assertActive(com2, parallel);
		assertSuspended(com1, false);
		assertSuspended(com2, false);
		events.assertEvents(0, 0, 0, 1, 1);
		events.assertCallbacks(0);

		// When
		com1.forceCompletion();
		com2.forceCompletion();

		assertActive(com1, false);
		assertActive(com2, false);
		assertSuspended(com1, false);
		assertSuspended(com2, false);
		events.assertEvents(1, 0, 0, 1, 1);
		events.assertCallbacks(1);
	}

	@Test(expected = IllegalStateException.class)
	public void testIllegalSuspension() {
		AsynchronousCommand async = new AsynchronousCommand();
		CommandProxy proxy = Commands.asSequence().add(async).execute();
		proxy.suspend();
	}

	@Test
	public void testSequentialTimeout() throws InterruptedException {
		FullCommand com1 = new FullCommand();
		FullCommand com2 = new FullCommand();
		assertTimeout(Commands.asSequence().add(com1).add(com2).timeout(100), com1, com2, false);
	}

	@Test
	public void testParallelTimeout() throws InterruptedException {
		FullCommand com1 = new FullCommand();
		FullCommand com2 = new FullCommand();
		assertTimeout(Commands.inParallel().add(com1).add(com2).timeout(100), com1, com2, true);
	}

	private void assertTimeout(CommandGroupBuilder builder, FullCommand com1, FullCommand com2, boolean parallel) throws InterruptedException {
		// Given
		CommandEventCounter events = new CommandEventCounter();
		addCallbacks(builder, events);
		CommandProxy proxy = builder.execute();
		events.setTarget(proxy);

		events.assertEvents(0);
		events.assertCallbacks(0);
		assertActive(com1, true);
		assertActive(com2, parallel);

		// When
		Thread.sleep(200);

		// Then
		events.assertEvents(0, 1);
		events.assertCallbacks(0, 1);
		assertThat(events.getException(), is(instanceOf(CommandException.class)));
		CommandException exception = (CommandException) events.getExceptionFromCallback();
		assertThat(exception.getCause(), is(instanceOf(CommandTimeoutException.class)));
		assertThat(exception.getExecutor(), sameInstance((CommandExecutor) proxy));
		assertThat(exception.getTarget(), is(instanceOf(CommandGroup.class)));
	}

	private void addCallbacks(CommandGroupBuilder builder, CommandEventCounter counter) {
		builder //
		.allResults(counter.resultCallback) //
				.cancel(counter.cancelCallback) //
				.exception(counter.exceptionCallback);
	}

}
