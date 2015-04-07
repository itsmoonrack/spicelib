package org.spicefactory.lib.command.impl;

import org.spicefactory.lib.command.callback.CancelCallback;

public class CancellableSwingCommand extends AsyncSwingCommand {

	public CancelCallback callback;

	public void cancel() {
		callback.cancel();
	}

}
