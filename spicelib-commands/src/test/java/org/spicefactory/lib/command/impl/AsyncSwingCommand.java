package org.spicefactory.lib.command.impl;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.spicefactory.lib.command.Async;
import org.spicefactory.lib.command.events.CommandEvent;
import org.spicefactory.lib.event.EventListener;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
@Async
public class AsyncSwingCommand implements EventListener<CommandEvent> {

	private final Lock lock = new ReentrantLock();
	private final Condition race = lock.newCondition();
	private final Condition execute = lock.newCondition();
	private final Condition complete = lock.newCondition();

	public static AsyncSwingCommand lastCreated;

	public Object result;
	public boolean executed;
	private boolean executing;

	public AsyncSwingCommand() {
		lastCreated = this;
	}

	// Executed in SwingWorker thread.
	public Object execute() throws InterruptedException {
		try {
			lock.lock();
			executing = true;
			race.signal();
			execute.await(); // Simulates an asynchronous blocking operation.
			executed = true;
			return result;
		}
		finally {
			lock.unlock();
		}
	}

	// Executed in Test thread.
	public void invokeCallback(Object param) {
		try {
			lock.lock();
			result = param;
			if (!executing) {
				race.await(); // Avoids race conditions.
			}
			execute.signal();
			complete.await(); // Blocks the testing thread.
		}
		catch (InterruptedException e) {

		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public void process(CommandEvent event) {
		try {
			lock.lock();
			complete.signal(); // Releases the testing thread.
		}
		finally {
			lock.unlock();
		}
	}
}
