package org.spicefactory.lib.command.builder;

/**
 * Entry point for the build DSL for configuring and creating new command instances.
 * <p>
 * This DSL exists mainly for convenience. Any command types built by this class can also be created by using their respective target APIs. But
 * using this DSL usually leads to code that is more concise.
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public final class Commands {

	/**
	 * Creates a builder for the specified command instance.
	 * <p>
	 * Legal parameters are any instances that implement either <code>Command</code> or <code>CommandBuilder</code>, or any other type in case an
	 * adapter is registered that knows how to turn the type into a command.
	 * </p>
	 * @return a new builder for the specified command instance
	 */
	public static AbstractCommandBuilder wrap(Object command) {
		return new CommandProxyBuilder(command);
	}

	/**
	 * Creates a builder for the specified command type.
	 * <p>
	 * The target type may either be a class that implements the <code>Command</code> interface itself or a type an adapter is registered for
	 * that knows how to turn the type into a command.
	 * </p>
	 * @return a new builder for the specified command type
	 */
	public static AbstractCommandBuilder create(Class<?> commandType) {
		return new CommandProxyBuilder(commandType);
	}

}
