package org.spicefactory.lib.command.impl;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.spicefactory.lib.command.callback.Callback;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class AsyncSwingCommand {

	final Lock lock = new ReentrantLock();
	final Condition complete = lock.newCondition();

	protected Callback<Object> callback;

	public static AsyncSwingCommand lastCreated;

	public boolean executed;

	public AsyncSwingCommand() {
		lastCreated = this;
	}

	public void execute(Callback<Object> callback) {
		this.callback = callback;
		try {
			lock.lock();
			complete.signal();
			executed = true;
		}
		finally {
			lock.unlock();
		}
	}

	public void invokeCallback(Object param) throws InterruptedException {
		try {
			lock.lock();
			complete.await();
			callback.result(param);
		}
		finally {
			lock.unlock();
		}
	}
}
