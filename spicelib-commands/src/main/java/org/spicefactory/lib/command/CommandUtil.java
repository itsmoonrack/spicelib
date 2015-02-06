package org.spicefactory.lib.command;

/**
 * Utility methods for determining the capabilities of a target command.
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public final class CommandUtil {

	/**
	 * Determines whether the target command can be cancelled.
	 * @param com the target command
	 * @return true if the target command can be cancelled
	 */
	public static boolean isCancellable(Command c) {
		if (c instanceof CommandExecutor) {
			return ((CommandExecutor) c).isCancellable();
		} else {
			return c instanceof CancellableCommand;
		}
	}

	/**
	 * Determines whether the target command can be suspended.
	 * @param com the target command
	 * @return true if the target command can be suspended
	 */
	public static boolean isSuspendable(Command c) {
		if (c instanceof CommandExecutor) {
			return ((CommandExecutor) c).isSuspendable();
		} else {
			return c instanceof SuspendableCommand;
		}
	}

}
