package saadadb.products.inference;

import java.util.Map;

import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public class ObservationKWDetector extends KWDetector {

	public ObservationKWDetector(
			Map<String, AttributeHandler> tableAttributeHandler) {
		super(tableAttributeHandler);
	}
	public ObservationKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> entryAttributeHandler) {
		super(tableAttributeHandler, entryAttributeHandler);
	}
	
	public AttributeHandler getCollNameAttribute(){
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for an obs_collection");
		return this.search(RegExp.COLLNAME_UCD, RegExp.COLLNAME_KW);
	}
	public AttributeHandler getTargetAttribute(){
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for a target_name");
		return this.search(RegExp.TARGET_UCD, RegExp.TARGET_KW);
	}
	public AttributeHandler getFacilityAttribute(){
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for a facility_name");
		return this.search(RegExp.FACILITY_UCD, RegExp.FACILITY_KW);
	}
	public AttributeHandler getInstrumentAttribute(){
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for an instrument_name");
		return this.search(RegExp.INSTRUMENT_UCD, RegExp.INSTRUMENT_KW);
	}

}
