package org.spicefactory.lib.event;

// @FunctionalInterface
public interface EventListener<E extends Event> extends java.util.EventListener {

	void process(E event);

}
