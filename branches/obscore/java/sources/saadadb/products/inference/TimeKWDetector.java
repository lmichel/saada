package saadadb.products.inference;

import java.util.Map;

import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public class TimeKWDetector extends KWDetector {

	public TimeKWDetector(Map<String, AttributeHandler> tableAttributeHandler) {
		super(tableAttributeHandler);
		// TODO Auto-generated constructor stub
	}
	public TimeKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> entryAttributeHandler) {
		super(tableAttributeHandler, entryAttributeHandler);
	}

	public AttributeHandler getTminName(){
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the start date");
		return this.search(RegExp.TIME_START_UCD, RegExp.TIME_START_KW);
	}
	public AttributeHandler getTmaxName(){
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the end date");
		return this.search(RegExp.TIME_END_UCD, RegExp.TIME_END_KW);
	}
	public AttributeHandler getExposureName(){
		return null;
	}

}
