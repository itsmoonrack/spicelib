package org.spicefactory.lib.command;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.spicefactory.lib.command.builder.CommandGroupBuilder;
import org.spicefactory.lib.command.builder.Commands;
import org.spicefactory.lib.command.callback.ResultCallback;
import org.spicefactory.lib.command.data.CommandData;
import org.spicefactory.lib.command.impl.AsynchronousCommand;
import org.spicefactory.lib.command.model.CommandModel;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class CommandDataTest {

	@Test
	public void testSingleCommand() {
		// Given
		AsynchronousCommand async = new AsynchronousCommand();
		ResultHandler handler = new ResultHandler();

		// When
		Commands.wrap(async).result(handler).execute();
		async.forceCompletion("foo");

		// Then
		assertThat(handler.result, equalTo("foo"));
	}

	@Test
	public void testSequence() {
		group(Commands.asSequence());
	}

	@Test
	public void testParallel() {
		group(Commands.inParallel());
	}

	private CommandData group(CommandGroupBuilder builder) {
		return group(builder, 2);
	}

	private CommandData group(CommandGroupBuilder builder, int numResults) {
		// Given
		AsynchronousCommand com1 = new AsynchronousCommand();
		AsynchronousCommand com2 = new AsynchronousCommand();
		ObjectResultHandler allResultsHandler = new ObjectResultHandler();
		IntegerResultHandler lastResultHandler = new IntegerResultHandler();

		// When
		builder.add(com1).add(com2).allResults(allResultsHandler).lastResult(lastResultHandler).execute();
		com1.forceCompletion("foo");
		com2.forceCompletion(7);

		// Then
		assertThat(lastResultHandler.result, equalTo(7));
		assertThat(allResultsHandler.result, is(instanceOf(CommandData.class)));
		CommandData data = (CommandData) allResultsHandler.result;
		assertThat(data.getObject(String.class), equalTo("foo"));
		assertThat(data.getObject(Integer.class), equalTo(7));
		assertThat(data.getObjects(), hasSize(numResults));
		return data;
	}

	@Test
	public void testPassThrough() {
		// Given
		CommandData data = group(Commands.asSequence().data(new Date()), 3);

		// Then
		assertThat(data.getObject(Date.class), notNullValue());
	}

	@Test
	public void testInjection() {
		// Given
		CommandModel model = new CommandModel("foo");
		AsynchronousCommand com1 = new AsynchronousCommand();

		// When
		Commands.asSequence().add(com1).create(AsynchronousCommand.class).execute();

		// Then
		assertFalse(model.isInjected());
		com1.forceCompletion(model);
		assertTrue(model.isInjected());
	}

	private class ResultHandler implements ResultCallback<String> {

		String result;

		@Override
		public void result(String result) {
			this.result = result;
		}

	}

	private class ObjectResultHandler implements ResultCallback<Object> {

		Object result;

		@Override
		public void result(Object result) {
			this.result = result;
		}

	}

	private class IntegerResultHandler implements ResultCallback<Integer> {

		Integer result;

		@Override
		public void result(Integer result) {
			this.result = result;
		}

	}

}
