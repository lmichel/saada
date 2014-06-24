package saadadb.products.inference;

import java.util.List;
import java.util.Map;

import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetter;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public class ObservationKWDetector extends KWDetector {

	public ObservationKWDetector(
			Map<String, AttributeHandler> tableAttributeHandler, List<String> comments) {
		super(tableAttributeHandler);
	}
	public ObservationKWDetector(Map<String, AttributeHandler> tableAttributeHandler
			, Map<String, AttributeHandler> entryAttributeHandler, List<String> comments) {
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
}
