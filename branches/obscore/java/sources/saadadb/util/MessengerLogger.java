
package saadadb.util;

import hecds.Logger;

/**
 * Decorator of the Messenger t be used by the HECDS package logger
 * @author michel
 * @version $Id$
 */
public class MessengerLogger implements Logger {

	@Override
	public void trace(String msg) {
		Messenger.printMsg(Messenger.TRACE, msg);
	}

	@Override
	public void debug(String msg) {
		Messenger.printMsg(Messenger.DEBUG, msg);
	}

	@Override
	public void error(String msg) {
		Messenger.printMsg(Messenger.ERROR, msg);
	}

	@Override
	public void warn(String msg) {
		Messenger.printMsg(Messenger.WARNING, msg);
	}

	@Override
	public void locatedMsg(String msg) {
		Messenger.printLocatedMsg(msg.toString());
	}
	
	@Override
	public void printStackTrace(Exception e) {
		Messenger.printStackTrace(e);
	}


}
