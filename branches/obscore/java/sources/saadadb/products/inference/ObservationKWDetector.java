package saadadb.products.inference;

import java.util.List;
import java.util.Map;

import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;

public class ObservationKWDetector extends KWDetector {

	public ObservationKWDetector(
			Map<String, AttributeHandler> tableAttributeHandler, List<String> comments) {
		super(tableAttributeHandler, null);
	}
	public ObservationKWDetector(Map<String, AttributeHandler> tableAttributeHandler
			, Map<String, AttributeHandler> entryAttributeHandler, List<String> comments) {
		super(tableAttributeHandler, entryAttributeHandler, null);
	}
	
	public ColumnExpressionSetter getCollectionName() throws Exception{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for an obs_collection");
		return this.search("obs_collection", RegExp.COLLNAME_UCD, RegExp.COLLNAME_KW);
	}
	public ColumnExpressionSetter getTargetName() throws Exception{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for a target_name");
		return this.search("target_name", RegExp.TARGET_UCD, RegExp.TARGET_KW);
	}
	public ColumnExpressionSetter getFacilityName() throws Exception{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for a facility_name");
		return this.search("facility_name", RegExp.FACILITY_UCD, RegExp.FACILITY_KW);
	}
	public ColumnExpressionSetter getInstrumentName() throws Exception{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for an instrument_name");
		return this.search("instrument_name", RegExp.INSTRUMENT_UCD, RegExp.INSTRUMENT_KW);
	}
	public ColumnExpressionSetter getObsPublisherDid() throws Exception{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for an obs_publisher_did: no automatic detection availbale");
		return new ColumnExpressionSetter("instrument_name");
	}
	public ColumnExpressionSetter getCalibLevel() throws Exception{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for an calib_level: no automatic detection availbale");
		return new ColumnExpressionSetter("calib_level");
	}
}
