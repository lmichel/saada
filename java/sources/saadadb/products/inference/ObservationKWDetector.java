package saadadb.products.inference;

import java.util.Map;

import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetter;
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
	
	public ColumnSetter getCollectionName() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for an obs_collection");
		return this.search(RegExp.COLLNAME_UCD, RegExp.COLLNAME_KW);
	}
	public ColumnSetter getTargetName() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for a target_name");
		return this.search(RegExp.TARGET_UCD, RegExp.TARGET_KW);
	}
	public ColumnSetter getFacilityName() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for a facility_name");
		return this.search(RegExp.FACILITY_UCD, RegExp.FACILITY_KW);
	}
	public ColumnSetter getInstrumentName() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for an instrument_name");
		return this.search(RegExp.INSTRUMENT_UCD, RegExp.INSTRUMENT_KW);
	}
	public ColumnSetter getObsIdComponents() throws FatalException{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for an obs_id");
		String obsid="";
		ColumnSetter cs;
		if( !(cs = this.getFacilityName()).notSet() ) obsid += cs.getValue();
		if( !(cs = this.getTargetName()).notSet() ){
			if( obsid.length() >= 0 ) obsid += " [";
			obsid += cs.getValue() + "]";
		}
		if( obsid.length() >= 0 ) {
			cs = new ColumnSetter(obsid, false, "Build with instrument_name and target_name");
			return cs;
		}
		return new ColumnSetter();
	}

}
