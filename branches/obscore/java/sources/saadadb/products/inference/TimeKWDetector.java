package saadadb.products.inference;

import java.util.Map;

import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetter;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public class TimeKWDetector extends KWDetector {

	public TimeKWDetector(Map<String, AttributeHandler> tableAttributeHandler) {
		super(tableAttributeHandler);
	}
	public TimeKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> entryAttributeHandler) {
		super(tableAttributeHandler, entryAttributeHandler);
	}

	public ColumnSetter getTMin() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the start date");
		return this.search(RegExp.TIME_START_UCD, RegExp.TIME_START_KW);
	}
	public ColumnSetter getTMax() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the end date");
		return this.search(RegExp.TIME_END_UCD, RegExp.TIME_END_KW);
	}
	public ColumnSetter getExpTime() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the exposure time");
		return this.search(RegExp.EXPOSURE_TIME_UCD, RegExp.EXPOSURE_TIME_KW);
	}
	public ColumnSetter getExposureName(){
		return null;
	}

}
