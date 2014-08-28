package saadadb.products.inference;

import java.util.List;
import java.util.Map;

import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;

public class TimeKWDetector extends KWDetector {
	private double timeref = SaadaConstant.DOUBLE;
	
	public TimeKWDetector(Map<String, AttributeHandler> tableAttributeHandler, List<String> comments) {
		super(tableAttributeHandler);
	}
	public TimeKWDetector(Map<String, AttributeHandler> tableAttributeHandler
			, Map<String, AttributeHandler> entryAttributeHandler, List<String> comments) {
		super(tableAttributeHandler, entryAttributeHandler);
	}

	/**
	 * @param retour
	 * @throws Exception 
	 */
	private void addTimeRef(ColumnExpressionSetter retour) throws Exception{
		try{
			double d= Double.parseDouble(retour.getValue());
			
			ColumnExpressionSetter ref = this.searchByName(RegExp.TIME_REF_KW);
			if( !ref.notSet() ) {
				double v = Double.parseDouble(ref.getValue());
				retour.completeMessage("Taken MJDREF as time ref (" + v + ")");
				v += d/(24*3600);
				retour.setValue(String.valueOf(v));
			}
		} catch (NumberFormatException e) {	}
	}
	
	/**
	 * @return
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getTMin() throws Exception{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the date start");
		ColumnExpressionSetter retour =  this.search(RegExp.TIME_START_UCD, RegExp.TIME_START_KW);
		if( retour.notSet() ) {
			ColumnExpressionSetter year = this.searchByName("YEAR");
			ColumnExpressionSetter month = this.searchByName("MONTH");
			ColumnExpressionSetter day = this.searchByName("DAY");
			if( !year.notSet() && !month.notSet() && !day.notSet() ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No date found but YEAR/MONTH/DAY");
				retour = new ColumnExpressionSetter();
				retour.setByValue(year.getValue() + "-" + month.getValue()  + "-" + day.getValue() , false);
				retour.completeMessage("Build from YEAR/MONTH/DAY keywords");
			} 
		} else {
			this.addTimeRef(retour);
		}
		return retour;
	}
	public ColumnExpressionSetter getTMax() throws Exception{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the date end");
		ColumnExpressionSetter retour =  this.search(RegExp.TIME_END_UCD, RegExp.TIME_END_KW);
		if( !retour.notSet() ){
			this.addTimeRef(retour);
		}
		return retour;
	}
	public ColumnExpressionSetter getExpTime() throws Exception{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the exposure time");
		return this.search(RegExp.EXPOSURE_TIME_UCD, RegExp.EXPOSURE_TIME_KW);
	}
	public ColumnExpressionSetter getExposureName(){
		return null;
	}

}