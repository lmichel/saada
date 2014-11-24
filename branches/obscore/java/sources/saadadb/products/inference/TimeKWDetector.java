package saadadb.products.inference;

import hecds.wcs.Modeler;
import hecds.wcs.types.AxeType;

import java.util.List;
import java.util.Map;

import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.products.setter.ColumnRowSetter;
import saadadb.products.setter.ColumnWcsSetter;
import saadadb.util.Messenger;
import saadadb.util.RegExpMatcher;
import saadadb.vocabulary.RegExp;

public class TimeKWDetector extends KWDetector {
	private ColumnExpressionSetter tminSetter;
	private ColumnExpressionSetter tmaxSetter;
	private ColumnExpressionSetter exptimeSetter;

	/**
	 * @param tableAttributeHandler
	 * @param wcsModeler
	 * @param comments
	 * @throws SaadaException 
	 */
	public TimeKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Modeler wcsModeler, List<String> comments) throws SaadaException {
		super(tableAttributeHandler, wcsModeler.getProjection(AxeType.TIME));
	}
	/**
	 * @param tableAttributeHandler
	 * @param entryAttributeHandler
	 * @param wcsModeler
	 * @param comments
	 * @throws SaadaException 
	 */
	public TimeKWDetector(Map<String, AttributeHandler> tableAttributeHandler
			, Map<String, AttributeHandler> entryAttributeHandler, Modeler wcsModeler, List<String> comments) throws SaadaException {
		super(tableAttributeHandler, entryAttributeHandler, wcsModeler.getProjection(AxeType.TIME));
	}


	/**
	 * @return
	 * @throws SaadaException
	 */
	private void detectAxeParams() throws SaadaException {	
		if( isMapped ){
			return;
		}
		this.isMapped = true;
		try {
			if( this.findTimeRangeByWCS() ||  this.findTimeRangeInColumns() || this.findTimeRangeInKeywords()  ) {
				return;
			}
		} catch (SaadaException e) {
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
		return;
	}

	/**
	 * Ask the WCS projection for time coordinates
	 * @return true if the dispersion has been found
	 * @throws Exception 
	 */
	private boolean findTimeRangeByWCS() throws Exception{
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching for time coordinate in WCS");
		if( this.projection.isUsable()){
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Found spectral coodinate in WCS");
			this.tminSetter     = new ColumnWcsSetter("t_min"    , "WCS.getMin(1)", this.projection);
			this.tminSetter     = new ColumnWcsSetter("t_max"    , "WCS.getMax(1)", this.projection);
			this.exptimeSetter    = new ColumnWcsSetter("t_exptime"   , "WCS.getNaxis(1)", this.projection);
			return true;
		} else {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "No time coodinate found in WCS");
			return false;
		}
	}

	/**
	 * The unit detection is not required by this method to succeed
	 * @return
	 * @throws Exception 
	 */
	private boolean findTimeRangeInKeywords() throws Exception{
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching time coordinates by UCDs or keyword ");
		this.tminSetter =  this.search("t_min", RegExp.TIME_START_UCD, RegExp.TIME_START_KW);
		if( this.tminSetter.isNotSet() ) {
			ColumnExpressionSetter year = this.searchByName("year", "YEAR");
			ColumnExpressionSetter month = this.searchByName("month", "MONTH");
			ColumnExpressionSetter day = this.searchByName("day", "DAY");
			if( !year.isNotSet() && !month.isNotSet() && !day.isNotSet() ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No date found but YEAR/MONTH/DAY");
				this.tminSetter = new ColumnExpressionSetter("t_min", "strcat(" 
						+ year.getSingleAttributeHandler().getNameorg() + "," + "'-'" + ","
						+ month.getSingleAttributeHandler().getNameorg() + "," + "'-'" + ","
						+ day.getSingleAttributeHandler().getNameorg() + ")" , this.tableAttributeHandler, false);
				this.tminSetter.completeMessage("Build from YEAR/MONTH/DAY keywords");
			} 
		} else {
			this.searchTimeRef(this.tminSetter);
		}

		this.tmaxSetter =  this.search("t_max", RegExp.TIME_END_UCD, RegExp.TIME_END_KW);
		if( !this.tmaxSetter.isNotSet() ){
			this.searchTimeRef(this.tmaxSetter);
		}
		this.exptimeSetter =  this.search("t_exptime", RegExp.EXPOSURE_TIME_UCD, RegExp.EXPOSURE_TIME_KW);

		int cpt = 0;
		if( !this.tminSetter.isNotSet() ) cpt++;
		if( !this.tmaxSetter.isNotSet() ) cpt++;
		if( !this.exptimeSetter.isNotSet() ) cpt++;
		return (cpt >= 2);
	}

	/**
	 * Look for the time coordinates in the table columns (if exist). 
	 * @return true if the dispersion has been found
	 * @throws IgnoreException 
	 */
	private boolean findTimeRangeInColumns() throws Exception{
		/*
		 * If no range set in params, try to find it out from fields
		 */	
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching time coordinate in the columns");
		if( this.entryAttributeHandler != null ){
			ColumnExpressionSetter timeSetter = this.searchColumns(null, RegExp.TIME_START_UCD, RegExp.TIME_START_KW);
			if( !timeSetter.isNotSet()  ){
				/*
				 * If a column has been found, the expression is the column name.
				 */
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Found column "+ timeSetter.getExpression());
				TimeRef tr = this.searchTimeRef(timeSetter);
				String expression = tr.getTimeRef();
				if( tr.convFactor != null ){
					expression +=  "Column.getMinValue(" + timeSetter.getExpression() + ")" + tr.convFactor;
				} else {
					this.tminSetter = new ColumnExpressionSetter("t_min");
					this.tminSetter.completeMessage("Cannot interpret the data format in column " + timeSetter.getExpression());
					this.tminSetter = new ColumnExpressionSetter("t_min");
					this.tminSetter.completeMessage("Cannot interpret the data format in column " + timeSetter.getExpression());
					return false;
				}	
				String message = "Take the expression result (" + expression + ")";
				this.tminSetter = new ColumnRowSetter("t_min", expression);
				this.tminSetter.completeMessage(message);
				
				expression =  tr.getTimeRef() + "Column.getMaxValue(" + timeSetter.getExpression() + ")" + tr.convFactor;
				this.tmaxSetter = new ColumnRowSetter("t_max", expression);
				this.tmaxSetter.completeMessage(message);
				return true;
			} 				
		}
		return  false;
	}

	/**
	 * @param retour
	 * @throws Exception 
	 */
	private TimeRef searchTimeRef(ColumnExpressionSetter timeSetter) throws Exception{
		TimeRef retour = new TimeRef();
		if( timeSetter.isNotSet() ){
			return retour;
		}
		/*
		 * We suppose that timeRef is in MJD
		 */
		ColumnExpressionSetter ref = this.searchByName("timeref", RegExp.TIME_REF_KW);
		String comment = timeSetter.getSingleAttributeHandler().getComment();
		if( !ref.isNotSet() ){
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "take KW " + ref.getSingleAttributeHandler().getNameorg() + " as time reference");
			retour.timeRef = ref.getSingleAttributeHandler().getNameorg();
		} else {
			RegExpMatcher rem = new RegExpMatcher(".*([0-9]{4}).*", 1);
			List<String> ls = rem.getMatches(comment);
			if( ls != null ) {
				retour.timeRef = "MJD('01-01-"  + ls.get(0) + "')";
			}
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "take " + retour.timeRef + " as time reference (infered from KW desc '" + comment+ "')");
		}


		RegExpMatcher rem = new RegExpMatcher(".*(?i)((?:seconds)|(?:hours)|(?:minutes)).*" , 1);
		
		List<String> ls = rem.getMatches(comment);
		if( ls != null ) {
			retour.convFactor(ls.get(0));
		}
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "take " + retour.convFactor + " as time conversion factor (infered from KW desc '" + comment+ "')");		
		return retour;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getTMin() throws Exception{
		this.detectAxeParams();
		System.out.println(this.tminSetter);
		return (this.tminSetter == null)? new ColumnExpressionSetter("t_min"): this.tminSetter;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public ColumnExpressionSetter getTMax() throws Exception{
		this.detectAxeParams();
		return (this.tmaxSetter == null)? new ColumnExpressionSetter("t_max"): this.tmaxSetter;
	}
	/**
	 * @return
	 * @throws Exception
	 */
	public ColumnExpressionSetter getExpTime() throws Exception{
		this.detectAxeParams();
		return (this.exptimeSetter == null)? new ColumnExpressionSetter("t_exptime"): this.exptimeSetter;
	}
	/**
	 * @return
	 * @throws SaadaException 
	 */
	public ColumnExpressionSetter getExposureName() throws SaadaException{
		this.detectAxeParams();
		return null;
	}

	/**
	 * @author michel
	 * @version $Id$
	 */
	class TimeRef{
		String timeRef = null;
		String convFactor  = null;

		void convFactor(String unit){
			if( unit.equalsIgnoreCase("seconds")) {
				convFactor = "/86400";
			} else if( unit.equalsIgnoreCase("minutes")) {
				convFactor = "/14400";
			} else if( unit.equalsIgnoreCase("hours")) {
				convFactor ="/24";
			} else if( unit.equalsIgnoreCase("days")) {
				convFactor = "";
			} else {
				convFactor = null;
			}
		}
		
		String getTimeRef() {
			return (this.timeRef == null || this.timeRef.length() == 0 )?"": (this.timeRef + " + ");
		}
	}

}
