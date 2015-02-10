package org.spicefactory.lib.command.result;

/**
 * Represents the registration for a single result processor.
 * <p>
 * Use the <code>exists</code> property to determine whether an actual processor had been registered
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class ResultProcessor {

	private Class<?> processorType;

	private final Class<?> type;

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	ResultProcessor(Class<?> type) {
		this.type = type;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Specifies the type of command to instantiate and use as a result processor for each matching result.
	 * <p>
	 * Instances of this type must be a command that can get executed through calling <code>Commands.wrap(instance).execute()</code>.
	 * @param type the type of command to instantiate and use as a result processor for each matching result
	 */
	public void processorType(Class<?> type) {
		processorType = type;
	}

	/**
	 * Indicates whether any command type of factory has been specified for this result processor registration.
	 */
	public boolean exists() {
		return processorType != null;
	}

	/**
	 * Indicates whether this result processor can handle the specified result.
	 * @param result the result produced by a command
	 * @return true when this result processor can handle the specified result
	 */
	public boolean supports(Object result) {
		return type.isAssignableFrom(result.getClass());
	}

	/**
	 * Creates a new instance of the result processor.
	 * @return a new instance of the result processor
	 */
	public Object newInstance() {
		if (!exists()) {
			throw new IllegalStateException("Neither type nor factory have been specified for this result processor");
		}
		try {
			return processorType.newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////
}
