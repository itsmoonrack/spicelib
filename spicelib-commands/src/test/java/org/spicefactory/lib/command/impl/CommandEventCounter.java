package org.spicefactory.lib.command.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.spicefactory.lib.command.AsyncCommand;
import org.spicefactory.lib.command.callback.CancelCallback;
import org.spicefactory.lib.command.callback.ExceptionCallback;
import org.spicefactory.lib.command.callback.ResultCallback;
import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.command.events.CommandResultEvent;
import org.spicefactory.lib.event.EventListener;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class CommandEventCounter {

	public final ResultCallback<Object> resultCallback = new ResultCallback<Object>() {
		@Override
		public void result(Object result) {
			resultCallback(result);
		}
	};
	public final ExceptionCallback<Throwable> exceptionCallback = new ExceptionCallback<Throwable>() {
		@Override
		public void exception(Throwable e) {
			exceptionCallback(e);
		}
	};
	public final CancelCallback cancelCallback = new CancelCallback() {
		@Override
		public void cancel() {
			cancelCallback();
		}
	};

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	private final List<Object> results = new LinkedList<Object>();
	private final List<Object> exceptions = new LinkedList<Object>();
	private final Map<Integer, AtomicInteger> events = new HashMap<Integer, AtomicInteger>();

	private final List<Object> resultCallbacks = new LinkedList<Object>();
	private final List<Object> exceptionCallbacks = new LinkedList<Object>();
	private final AtomicInteger cancelCallbacks = new AtomicInteger(0);

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	public CommandEventCounter() {
		//
	}

	public CommandEventCounter(AsyncCommand target) {
		setTarget(target);
	}

	public void resultCallback(Object result) {
		resultCallbacks.add(result);
	}

	public void exceptionCallback(Object exception) {
		exceptionCallbacks.add(exception);
	}

	public void cancelCallback() {
		cancelCallbacks.incrementAndGet();
	}

	public void assertEvents(int complete) {
		assertEvents(complete, 0, 0, 0, 0);
	}

	public void assertEvents(int complete, int exception) {
		assertEvents(complete, exception, 0, 0, 0);
	}

	public void assertEvents(int complete, int exception, int cancel) {
		assertEvents(complete, exception, cancel, 0, 0);
	}

	public void assertEvents(int complete, int exception, int cancel, int suspend) {
		assertEvents(complete, exception, cancel, suspend, 0);
	}

	public void assertEvents(int complete, int exception, int cancel, int suspend, int resume) {
		assertThat(eventCount(CommandResultEvent.COMPLETE), equalTo(complete));
		assertThat(eventCount(CommandResultEvent.EXCEPTION), equalTo(exception));
		assertThat(eventCount(CommandEvent.CANCEL), equalTo(cancel));
		assertThat(eventCount(CommandEvent.SUSPEND), equalTo(suspend));
		assertThat(eventCount(CommandEvent.RESUME), equalTo(resume));
	}

	public void assertCallbacks(int complete) {
		assertCallbacks(complete, 0, 0);

	}

	public void assertCallbacks(int complete, int exception) {
		assertCallbacks(complete, exception, 0);
	}

	public void assertCallbacks(int complete, int exception, int cancel) {
		assertThat(resultCallbacks, hasSize(complete));
		assertThat(exceptionCallbacks, hasSize(exception));
		assertThat(cancelCallbacks.get(), equalTo(cancel));
	}

	public void setTarget(AsyncCommand value) {
		value.addEventListener(CommandResultEvent.COMPLETE, new ResultEventHandler());
		value.addEventListener(CommandResultEvent.EXCEPTION, new ResultEventHandler());
		value.addEventListener(CommandEvent.CANCEL, new EventHandler());
		value.addEventListener(CommandEvent.SUSPEND, new EventHandler());
		value.addEventListener(CommandEvent.RESUME, new EventHandler());
	}

	public int eventCount(int type) {
		return events.containsKey(type) ? events.get(type).intValue() : 0;
	}

	public Object getException() {
		assertThat(eventCount(CommandResultEvent.EXCEPTION), equalTo(1));
		assertThat(exceptionCallbacks, hasSize(1));
		assertThat(exceptionCallbacks.get(0), sameInstance(exceptions.get(0)));
		return exceptions.get(0);
	}

	public Object getExceptionFromCallback() {
		assertThat(exceptionCallbacks, hasSize(1));
		return exceptionCallbacks.get(0);
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	private void handleResultEvent(CommandResultEvent event) {
		List<Object> targets = event.getID() == CommandResultEvent.COMPLETE ? results : exceptions;
		targets.add(event.getValue());
		handleEvent(event);
	}

	private void handleEvent(CommandEvent event) {
		if (events.containsKey(event.getID())) {
			events.get(event.getID()).incrementAndGet();
		} else {
			events.put(event.getID(), new AtomicInteger(1));
		}
	}

	private class ResultEventHandler implements EventListener<CommandEvent> {
		@Override
		public void process(CommandEvent event) {
			handleResultEvent((CommandResultEvent) event);
		}
	}

	private class EventHandler implements EventListener<CommandEvent> {
		@Override
		public void process(CommandEvent event) {
			handleEvent(event);
		}
	}

}
