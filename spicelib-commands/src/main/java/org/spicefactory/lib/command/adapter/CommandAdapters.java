package org.spicefactory.lib.command.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Central registry for all available command adapters.
 * <p>
 * An adapter must be registered before executing or configuring one of the commands handled by the adapter.
 * </p>
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public final class CommandAdapters {

	private static final List<FactoryRegistration> factories = new LinkedList<FactoryRegistration>();

	/////////////////////////////////////////////////////////////////////////////
	// Package-private.
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Public API.
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Adds a factory to this registry.
	 * <p>
	 * For each new command instance that does not implement one of the command interfaces the factories get asked to create a new adapter in the
	 * specified order until one factory was able to handle the command (Chain of Responsibility).
	 * </p>
	 * @param the factory to add to this registry.
	 */
	public static void addFactory(CommandAdapterFactory factory) {
		addFactory(factory, Integer.MAX_VALUE);
	}

	/**
	 * Adds a factory to this registry.
	 * <p>
	 * The order attributes allows to sort all available factories. For each new command instance that does not implement one of the command
	 * interfaces the factories get asked to create a new adapter in the specified order until one factory was able to handle the command (Chain
	 * of Responsibility).
	 * </p>
	 * @param the factory to add to this registry.
	 * @param order the order to use for this factory
	 */
	public static void addFactory(CommandAdapterFactory factory, int order) {
		factories.add(new FactoryRegistration(factory, order));
		Collections.sort(factories);
	}

	/**
	 * Creates a new adapter for the specified target command.
	 * <p>
	 * Throws an error if the instance cannot be handled by any of the available adapters.
	 * </p>
	 * @param instance the target command that usually does not implement one of the Command interfaces
	 * @return a new adapter for the specified target command
	 */
	public static CommandAdapter createAdapter(Object instance) {
		CommandAdapter adapter;
		for (FactoryRegistration reg : factories) {
			adapter = reg.factory.createAdapter(instance);
			if (adapter != null) {
				return adapter;
			}
		}
		throw new IllegalStateException("No command adapter factory registered for instance " + instance);
	}

	/////////////////////////////////////////////////////////////////////////////
	// Internal implementation.
	/////////////////////////////////////////////////////////////////////////////

	private static class FactoryRegistration implements Comparable<FactoryRegistration> {

		private final int order;
		private final CommandAdapterFactory factory;

		public FactoryRegistration(CommandAdapterFactory factory, int order) {
			this.factory = factory;
			this.order = order;
		}

		@Override
		public int compareTo(FactoryRegistration o) {
			return order - o.order;
		}
	}
}
