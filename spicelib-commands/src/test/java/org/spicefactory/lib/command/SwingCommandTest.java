package org.spicefactory.lib.command;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spicefactory.lib.command.adapter.CommandAdapters;
import org.spicefactory.lib.command.builder.CommandProxyBuilder;
import org.spicefactory.lib.command.builder.Commands;
import org.spicefactory.lib.command.events.CommandException;
import org.spicefactory.lib.command.impl.AsyncResultProcessor;
import org.spicefactory.lib.command.impl.AsyncSwingCommand;
import org.spicefactory.lib.command.impl.CommandEventCounter;
import org.spicefactory.lib.command.impl.CommandWithProcessor;
import org.spicefactory.lib.command.impl.SyncResultProcessor;
import org.spicefactory.lib.command.impl.SyncSwingCommand;
import org.spicefactory.lib.command.impl.SyncSwingConstructorInjectionCommand;
import org.spicefactory.lib.command.impl.SyncSwingDataCommand;
import org.spicefactory.lib.command.impl.SyncSwingOptionalDataCommand;
import org.spicefactory.lib.command.model.AsyncResult;
import org.spicefactory.lib.command.model.CommandModel;
import org.spicefactory.lib.command.proxy.CommandProxy;
import org.spicefactory.lib.command.result.ResultProcessors;
import org.spicefactory.lib.command.swing.SwingCommandAdapterFactory;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class SwingCommandTest {

	private CommandEventCounter events;
	private CommandProxy proxy;

	@BeforeClass
	public static void addProcessor() {
		if (!ResultProcessors.forResultType(AsyncResult.class).exists()) {
			ResultProcessors.forResultType(AsyncResult.class).processorType(AsyncResultProcessor.class);
		}
		if (!ResultProcessors.forResultType(CommandWithProcessor.class).exists()) {
			ResultProcessors.forResultType(CommandWithProcessor.class).processorType(SyncResultProcessor.class);
		}
		CommandAdapters.addFactory(new SwingCommandAdapterFactory());
	}

	@Before
	public void setup() {
		events = new CommandEventCounter();
	}

	@Test
	public void testSynchronousCommand() {
		// Given
		SyncSwingCommand sync = new SyncSwingCommand();
		build(sync);
		assertInactive();

		// When
		proxy.execute();

		// Then
		assertCompleted();
		assertThat(sync.executed, is(true));
	}

	@Test
	public void testSynchronousException() {
		// Given
		SyncSwingDataCommand sync = new SyncSwingDataCommand();
		useBuilder(Commands.wrap(sync).data(new CommandModel("foo")));
		assertInactive();

		// When
		proxy.execute();

		// Then
		assertThat(sync.model, notNullValue());
		assertResult("foo");
	}

	@Test
	public void testSynchronousReturnValue() {
		// Given
		SyncSwingDataCommand sync = new SyncSwingDataCommand();
		useBuilder(Commands.wrap(sync).data(new CommandModel("foo")));
		assertInactive();

		// When
		proxy.execute();

		// Then
		assertThat(sync.model, notNullValue());
		assertResult("foo");
	}

	@Test
	public void testMissingRequiredParameter() {
		// Given
		SyncSwingDataCommand sync = new SyncSwingDataCommand(true);
		build(sync);
		assertInactive();

		// When
		proxy.execute();

		// Then
		assertThat(sync.model, nullValue());
		assertException(IllegalStateException.class);
	}

	@Test
	public void testMissingOptionalParameter() {
		// Given
		SyncSwingOptionalDataCommand sync = new SyncSwingOptionalDataCommand();
		build(sync);
		assertInactive();

		// When
		proxy.execute();

		// Then
		assertThat(sync.model, nullValue());
		assertCompleted();
	}

	@Test
	public void testOptionalParameter() {
		// Given
		SyncSwingOptionalDataCommand sync = new SyncSwingOptionalDataCommand();
		useBuilder(Commands.wrap(sync).data(new CommandModel("foo")));
		assertInactive();

		// When
		proxy.execute();

		// Then
		assertThat(sync.model, notNullValue());
		assertResult("foo");
	}

	@Test
	public void testConstructorInjectionMissingOptional() {
		// Given
		useBuilder(Commands //
				.create(SyncSwingConstructorInjectionCommand.class) //
				.data(new CommandModel("foo")) //
		);
		assertInactive();

		// When
		proxy.execute();

		// Then
		assertResult("foo:");
	}

	@Test
	public void testConstructorInjectionMissingRequired() {
		// Given
		useBuilder(Commands //
				.create(SyncSwingConstructorInjectionCommand.class) //
		);
		assertInactive();

		// When
		proxy.execute();

		// Then
		assertException(IllegalStateException.class);
	}

	@Test
	public void testAsynchronousCommand() throws InterruptedException {
		// Given
		AsyncSwingCommand async = new AsyncSwingCommand();
		build(async);
		assertInactive();

		// When
		proxy.execute();

		// Then
		assertActive();
		async.invokeCallback("foo");
		assertResult("foo");
	}

	@Test
	public void testCancellationOnTarget() {

	}

	private void build(Object com) {
		proxy = prepare(Commands.wrap(com)).build();
		events.setTarget(proxy);
	}

	private void useBuilder(CommandProxyBuilder builder) {
		proxy = prepare(builder).build();
		events.setTarget(proxy);
	}

	private CommandProxyBuilder prepare(CommandProxyBuilder builder) {
		builder //
		.result(events.resultCallback) //
				.cancel(events.cancelCallback) //
				.exception(events.exceptionCallback); //
		return builder;
	}

	private void assertInactive() {
		assertThat(proxy.isActive(), is(false));
		events.assertEvents(0);
		events.assertCallbacks(0);
	}

	private void assertActive() {
		assertThat(proxy.isActive(), is(true));
		events.assertEvents(0);
		events.assertCallbacks(0);
	}

	private void assertCompleted() {
		events.assertEvents(1);
		events.assertCallbacks(1);
	}

	private void assertResult(Object value) {
		assertCompleted();
		assertThat(events.getResult(), equalTo(value));
	}

	private void assertException(Class<?> expectedCause) {
		events.assertEvents(0, 1);
		events.assertCallbacks(0, 1);
		assertThat(events.getException(), is(instanceOf(CommandException.class)));
		CommandException exception = (CommandException) events.getException();
		assertThat(exception.getCause(), is(instanceOf(expectedCause)));
		assertThat(exception.getExecutor(), sameInstance((CommandExecutor) proxy));
		assertThat(exception.getTarget(), sameInstance(proxy.getTarget()));
	}

}
