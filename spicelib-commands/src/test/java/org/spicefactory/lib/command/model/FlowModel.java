package org.spicefactory.lib.command.model;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.junit.Assert.assertThat;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Sylvain Lecoy <sylvain.lecoy@swissquote.ch>
 */
public class FlowModel {

	private List<String> commands = new LinkedList<String>();

	public void addCommand(String id) {
		commands.add(id);
	}

	public void assertFlow(String... values) {
		if (values.length > 0) {
			assertThat(commands, contains(values));
		} else {
			assertThat(commands, emptyCollectionOf(String.class));
		}
	}
}
