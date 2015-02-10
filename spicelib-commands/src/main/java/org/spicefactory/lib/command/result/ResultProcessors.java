package org.spicefactory.lib.command.result;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.spicefactory.lib.command.builder.CommandProxyBuilder;
import org.spicefactory.lib.command.builder.Commands;

/**
 * Central registry for all available result processors.
 * <p>
 * A processor must be registered before executing a command that produces a result that should be handled by the processor.
 * <p>
 * A result processor is a command itself and can be built with any of the available command implementation styles, including swing commands. The
 * result itself may get passed to the execute method the same way as data from preceding commands can get passed to a regular command.
 * </p>
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class ResultProcessors {

	private static final ConcurrentMap<Class<?>, ResultProcessor> byCommandType = new ConcurrentHashMap<Class<?>, ResultProcessor>();
	private static final ConcurrentMap<Class<?>, ResultProcessor> byResultType = new ConcurrentHashMap<Class<?>, ResultProcessor>();

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the result processor registration for the specified command type.
	 * <p>
	 * Such a processor processes all results produced by commands of this type (or any sub-type), no matter what the type of the actual result
	 * is.
	 * </p>
	 * @param type the type of the command
	 * @return the registration for the result processor
	 */
	public static ResultProcessor forCommandType(Class<?> type) {
		ResultProcessor result = byCommandType.get(type);
		if (result == null) {
			final ResultProcessor value = new ResultProcessor(type);
			result = byCommandType.putIfAbsent(type, value);
			if (result == null) {
				result = value;
			}
		}
		return result;
	}

	/**
	 * Returns the result processor registration for the specified result type.
	 * <p>
	 * Such a processor processes all results of this type (or any sub-type), no matter what the type of the command that produced the result.
	 * </p>
	 * @param type the type of the result
	 * @return the registration for the result processor
	 */
	public static ResultProcessor forResultType(Class<?> type) {
		ResultProcessor result = byResultType.get(type);
		if (result == null) {
			final ResultProcessor value = new ResultProcessor(type);
			result = byResultType.putIfAbsent(type, value);
			if (result == null) {
				result = value;
			}
		}
		return result;
	}

	/**
	 * Returns a new processor for the specified command and result or null if no matching processor was registered.
	 * @param command the command that produced the result
	 * @param result the result value
	 * @return a new processor for the specified command and result or null if no matching processor was registered
	 */
	public static CommandProxyBuilder newProcessor(Object command, Object result) {
		Object processor = createProcessor(command, result);

		if (processor == null)
			return null;

		return Commands.wrap(processor).data(result);
	}

	private static Object createProcessor(Object command, Object result) {
		for (ResultProcessor processor : byResultType.values()) {
			if (processor.supports(result)) {
				return processor.newInstance();
			}
		}
		for (ResultProcessor processor : byCommandType.values()) {
			if (processor.supports(result)) {
				return processor.newInstance();
			}
		}

		return null;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
