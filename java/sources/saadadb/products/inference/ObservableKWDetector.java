package saadadb.products.inference;

import java.util.Map;

import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetter;
import saadadb.util.Messenger;

/**

 * @author michel
 * @version $Id$
 */
public class ObservableKWDetector extends KWDetector {

	public ObservableKWDetector(Map<String, AttributeHandler> tableAttributeHandler) {
		super(tableAttributeHandler);
	}
	public ObservableKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> entryAttributeHandler) {
		super(tableAttributeHandler, entryAttributeHandler);
	}

	public ColumnSetter getUcdName() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the Observable UCD");
		return new ColumnSetter();
	}
	public ColumnSetter getUnitName() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the Observable UCD");
		return new ColumnSetter();
	}
	public ColumnSetter getCalibStatus() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the Observable calib status");
		return new ColumnSetter();
	}

}
