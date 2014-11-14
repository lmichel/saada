package saadadb.products.inference;

import hecds.wcs.Modeler;
import hecds.wcs.types.AxeType;

import java.util.List;
import java.util.Map;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.products.setter.ColumnRowSetter;
import saadadb.products.setter.ColumnWcsSetter;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;
import saadadb.util.SaadaConstant;

public class TimeKWDetector extends KWDetector {
	private double timeref = SaadaConstant.DOUBLE;
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
		this.mapCollectionSpectralCoordinateAuto();
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
		this.mapCollectionSpectralCoordinateAuto();
	}


	/**
	 * @return
	 * @throws SaadaException
	 */
	private boolean mapCollectionSpectralCoordinateAuto() throws SaadaException {	
		try {
			if( this.findTimeRangeByWCS() ||  this.findTimeRangeInColumns() || this.findTimeRangeInKeywords()  ) {
				return true;
			}
		} catch (SaadaException e) {
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
		return false;
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
		if( this.tminSetter.notSet() ) {
			ColumnExpressionSetter year = this.searchByName("year", "YEAR");
			ColumnExpressionSetter month = this.searchByName("month", "MONTH");
			ColumnExpressionSetter day = this.searchByName("day", "DAY");
			if( !year.notSet() && !month.notSet() && !day.notSet() ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No date found but YEAR/MONTH/DAY");
				this.tminSetter = new ColumnExpressionSetter("t_min", "strcat(" 
						+ year.getSingleAttributeHandler().getNameorg() + "," + "-"
						+ month.getSingleAttributeHandler().getNameorg() + "," + "-"
						+ day.getSingleAttributeHandler().getNameorg() + "," , this.tableAttributeHandler, false);
				this.tminSetter.completeMessage("Build from YEAR/MONTH/DAY keywords");
			} 
		} else {
			this.addTimeRef(this.tminSetter);
		}

		this.tmaxSetter =  this.search("t_max", RegExp.TIME_END_UCD, RegExp.TIME_END_KW);
		if( !this.tmaxSetter.notSet() ){
			this.addTimeRef(this.tmaxSetter);
		}
		this.exptimeSetter =  this.search("t_exptime", RegExp.EXPOSURE_TIME_UCD, RegExp.EXPOSURE_TIME_KW);

		int cpt = 0;
		if( !this.tminSetter.notSet() ) cpt++;
		if( !this.tmaxSetter.notSet() ) cpt++;
		if( !this.exptimeSetter.notSet() ) cpt++;
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
			ColumnExpressionSetter ah = this.searchColumns(null, RegExp.TIME_START_UCD, RegExp.TIME_START_KW);
			if( !ah.notSet()  ){
				/*
				 * If a column has been found, the expression is the column name.
				 */
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Found column "+ ah.getExpression());
				this.tminSetter = new ColumnRowSetter("t_min", "Column.getMinValue(" + ah.getExpression() + ")");
				this.tminSetter.completeMessage("Column "+ ah.getExpression() + " taken as time axe");
				this.tmaxSetter = new ColumnRowSetter("t_max", "Column.getMaxValue(" + ah.getExpression() + ")");
				this.tmaxSetter.completeMessage("Column "+ ah.getExpression() + " taken as time axe");
				return true;
			} 				
		}
		return  false;
	}

	/**
	 * @param retour
	 * @throws Exception 
	 */
	private void addTimeRef(ColumnExpressionSetter retour) throws Exception{
		try{
			double d= Double.parseDouble(retour.getValue());

			ColumnExpressionSetter ref = this.searchByName("timeref", RegExp.TIME_REF_KW);
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
		System.out.println(this.tminSetter);
		return (this.tminSetter == null)? new ColumnExpressionSetter("t_min"): this.tminSetter;
	}
	
	/**
	 * @return
	 * @throws Exception
	 */
	public ColumnExpressionSetter getTMax() throws Exception{
		return (this.tmaxSetter == null)? new ColumnExpressionSetter("t_max"): this.tmaxSetter;
	}
	/**
	 * @return
	 * @throws Exception
	 */
	public ColumnExpressionSetter getExpTime() throws Exception{
		return (this.exptimeSetter == null)? new ColumnExpressionSetter("t_exptime"): this.exptimeSetter;
	}
	/**
	 * @return
	 */
	public ColumnExpressionSetter getExposureName(){
		return null;
	}

}
